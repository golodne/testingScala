package Actor


import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{Matchers, WordSpecLike}

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration


case class PhotoMessage(id: String,
                 photo: String,
                 creationTime: Option[String] = None,
                 speed: Option[Int] = None)

case class TimeoutMessage(msg: PhotoMessage)


class ParallelActorsTest extends TestKit(ActorSystem("MySpec"))
  with WordSpecLike
  with Matchers
  with StopSystemAfterAll {

    "ParrallelActorsTest" must {
      "send msg to controller speed" in {
         val endProbe1 = TestProbe()
         val endProbe2 = TestProbe()
         val endProbe3 = TestProbe()

         val list = Seq(endProbe1.ref, endProbe2.ref, endProbe3.ref)
         //create new actor for send message
         val actorRef = system.actorOf(Props(new RecipientList(list)))
         val msg = "test message"
         actorRef ! msg

         endProbe1.expectMsg(msg)
         endProbe2.expectMsg(msg)
         endProbe3.expectMsg(msg)
      }

      " test Aggregator validate combine message" in {

        val resultProbe = TestProbe(); //result aggregator actor

        import scala.concurrent.duration._

        val aggrActor = system.actorOf(Props(new Aggrgator(1 seconds, resultProbe.ref)))

        //первое сообщение это время
        val msg11 = PhotoMessage("id1","152ka",Some("10 утра"),None)
        //второе сообщение это скорость
        val msg12 = PhotoMessage("id1","152ka",None,Some(100))

        aggrActor ! msg11
        aggrActor ! msg12

        val resultCombine = PhotoMessage(
          "id1",
          msg11.photo,
          msg11.creationTime,
          msg12.speed
        )

        resultProbe.expectMsg(resultCombine)

      }

      "test Aggregator with timeout, you must get first record" in {

        val resultProbe = TestProbe(); //result aggregator actor

        import scala.concurrent.duration._

        val aggrActor = system.actorOf(Props(new Aggrgator(1 seconds, resultProbe.ref)))

        //первое сообщение это время
        val msg11 = PhotoMessage("id1","152ka",Some("10 утра"),None)

        aggrActor ! msg11
        Thread.sleep(2000)

        val resultCombine = PhotoMessage(
          "id1",
          msg11.photo,
          msg11.creationTime,
          msg11.speed
        )

        resultProbe.expectMsg(resultCombine)


      }

      "test all functional" in {
        val endProbe = TestProbe()
        import scala.concurrent.duration._

        val aggrActor = system.actorOf(Props(new Aggrgator(1 seconds, endProbe.ref))) //создать агрегатор
        val speedRef = system.actorOf(Props(new GetSpeed(aggrActor)))
        val timeRef = system.actorOf(Props(new GetTime(aggrActor)))

        val recipientRef = system.actorOf(Props(new RecipientList(Seq(speedRef,timeRef))))

        val msg = PhotoMessage("id1","152ka")
        recipientRef ! msg

        //found Actor.PhotoMessage(id1,152ka,Some(10.09.2018T03:02:01),Some(50))
        val combimeResult = PhotoMessage(
          "id1",
          msg.photo,
          Some("10.09.2018T03:02:01"),
          Some(50)
        )

        endProbe.expectMsg(combimeResult)


      }

    }

}

//класс Агрегатора сообщений
class Aggrgator(timeout: FiniteDuration, pipe: ActorRef) extends Actor {

  val messages = new ListBuffer[PhotoMessage]
  /*
  implicit val ec = context.system.dispatcher

  override def preRestart(reason: Throwable, message: Option[Any]): Unit = {
    super.preRestart(reason,message)
    messages.foreach(self ! _)
    messages.clear()
  }
  */

  override def receive: Receive = {

    case TimeoutMessage(x) => {
      messages.find(_.id == x.id) match {
        case Some(findRec) =>
          {
             //отправить хотябы первое сообщение
             pipe ! findRec
             //удалить из буфера сообщение
             messages -= findRec
          }
        case None =>
      }
    }

    case x: PhotoMessage => {

      messages.find(_.id == x.id) match {
        case Some(msgInList) => //необходимо объединить сообщения и очистить буфер
          val combineMessage = new PhotoMessage(
            msgInList.id,
            msgInList.photo,
            msgInList.creationTime.orElse(x.creationTime),
            msgInList.speed.orElse(x.speed)
          )
          //отправить на фильтр проверки скорости
          pipe ! combineMessage
          //удалить из буфера сообщение
          messages -= msgInList

        case None => //first message (not found in buffer)
        {
          messages += x
          //запланировать таймаут самому себе
          context.system.scheduler.scheduleOnce(timeout, self, new TimeoutMessage(x))
        }

      }

    }

  }
}


class RecipientList(recipientList: Seq[ActorRef]) extends Actor {
  override def receive: Receive = {
    case msg: AnyRef =>
      recipientList.foreach(_ ! msg)
  }
}

object ProcessController {

  def processSpeed(photo: String) = {
    Thread.sleep(2000)
    50
  }

  def processTime(photo: String) = {
    Thread.sleep(1000)
    "10.09.2018T03:02:01"
  };
}

class GetSpeed(pipe: ActorRef) extends Actor {
  override def receive: Receive = {
    case msg: PhotoMessage =>
      pipe ! msg.copy(speed = Some(ProcessController.processSpeed(msg.photo)))
  }
}

class GetTime(pipe: ActorRef) extends Actor {
  override def receive: Receive = {
    case msg: PhotoMessage =>
      pipe ! msg.copy(creationTime = Some(ProcessController.processTime(msg.photo)))
  }
}
