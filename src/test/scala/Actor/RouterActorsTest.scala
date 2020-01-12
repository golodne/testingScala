package Actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{Matchers, WordSpecLike}

import scala.collection.mutable.ListBuffer

class RouteTest extends TestKit(ActorSystem("MySpec"))
  with WordSpecLike
  with Matchers
  with StopSystemAfterAll {

    "test Actor.RouteTest " must {
      "sending to actors route by option " in {

        val probe = TestProbe()
        val startRouteRef = system.actorOf(Props(new StartOrderRouter(probe.ref)),"startRouter")
        val minimalOrder = new Order(Seq())
        startRouteRef ! minimalOrder

        val defaultCar = Car(
          "black",
          false,
          false
        );

        probe.expectMsg(defaultCar)

        val superOrder = Order(Seq(CarOption.CAR_COLOR_GRAY, CarOption.NAVIGATION, CarOption.PARKING_SENSORS))
        startRouteRef ! superOrder

        val superCar = Car(
          "gray",
          true,
          true
        )

        probe.expectMsg(superCar)
      }
    }

  }

object CarOption extends Enumeration {
  val CAR_COLOR_GRAY, NAVIGATION, PARKING_SENSORS = Value
}

case class Order(options: Seq[CarOption.Value])
//aggregate message in process
case class Car(color: String = "",
               hasNavigation : Boolean = false,
               hasParkingSensors: Boolean = false)

case class RouteSlipMessage(routeSlip: Seq[ActorRef], message: AnyRef) //сообщение между задачами роутинга

trait RouteSlip {
  def sendMessageToNextgTask(routeSlip: Seq[ActorRef], message: AnyRef) = {
    val nextTask = routeSlip.head
    val nextSlip = routeSlip.tail
    if (nextSlip.isEmpty){
      nextTask ! message //последняя точка сообщение посылается без маршрута
    } else {
      //Послать сообщение на следующий шаг и обновить маршрут
      nextTask ! RouteSlipMessage(routeSlip = nextSlip, message = message)
    }
  }
}

class PaintCar(userColor: String) extends Actor with RouteSlip {
  override def receive: Receive = {
    case RouteSlipMessage(listRoutsActors,msg: Car) =>
      sendMessageToNextgTask(listRoutsActors, msg.copy(color = userColor))
  }
}

class AddNavigation() extends Actor with RouteSlip {
  override def receive: Receive = {
    case RouteSlipMessage(listRoutsActors, car: Car) =>
      sendMessageToNextgTask(listRoutsActors, car.copy(hasNavigation = true))
  }
}

class AddParkingSensors() extends Actor with RouteSlip {
  override def receive: Receive = {
    case RouteSlipMessage(list, car: Car) =>
      sendMessageToNextgTask(list, car.copy(hasParkingSensors = true))
  }
}


class StartOrderRouter(endStep: ActorRef) extends Actor with RouteSlip {
  //register all acters
  val paintBlackRef = context.actorOf(Props(new PaintCar("black")),"paintBlack")
  val paintGrayRef = context.actorOf(Props(new PaintCar("gray")),"paintGray")
  val addNavigationRef = context.actorOf(Props(new AddNavigation()),"navigation")
  val addParkingSensorsRef = context.actorOf(Props(new AddParkingSensors()),"parkingSensors")

  override def receive: Receive = {
    case o: Order =>
      val routeSlip = createRouteSlip(o.options)
      sendMessageToNextgTask(routeSlip,new Car)
  }


  //Actor.Order(potions: Seq[Actor.CarOption.Value])
  private def createRouteSlip(option: Seq[CarOption.Value]): Seq[ActorRef] = {

    val routeSlip = new ListBuffer[ActorRef]
    //автомобиль должен окрашиваться всегда
    if (!option.contains(CarOption.CAR_COLOR_GRAY)){
      routeSlip += paintBlackRef
    }

    option.foreach{
      case CarOption.CAR_COLOR_GRAY => routeSlip += paintGrayRef
      case CarOption.NAVIGATION => routeSlip += addNavigationRef
      case CarOption.PARKING_SENSORS => routeSlip += addParkingSensorsRef
    }

    routeSlip += endStep

    routeSlip
  }

}









class RouterActorsTest {

}



