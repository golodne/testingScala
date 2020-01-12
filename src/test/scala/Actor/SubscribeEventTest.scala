package Actor

import akka.actor.ActorSystem
import akka.testkit.{TestKit, TestProbe}
import org.scalatest.{Matchers, WordSpecLike}

//testOnly Actor.SubscribeEventTest

class SubscribeEventTest extends TestKit(ActorSystem("MySpec"))
  with WordSpecLike
  with Matchers
  with StopSystemAfterAll
{

  "Event Stream " must {
    "subscribe 2 test system, after published gets message" in {

      //2 systems
      val DeliverOrder = TestProbe();
      val giftModule = TestProbe();

      //subscribe on event with class
      system.eventStream.subscribe(DeliverOrder.ref,classOf[SomeMessage])
      system.eventStream.subscribe(giftModule.ref,classOf[SomeMessage])

      val msg = new SomeMessage("me",Some("Test Description"), 3)
      //val msg2 = new SomeMessage("me",Some("Test1"),2)

      //publishing
      system.eventStream.publish(msg)

      DeliverOrder.expectMsg(msg)
      giftModule.expectMsg(msg)


    }
  }

}

case class SomeMessage(name: String, description: Option[String], count: Int)


