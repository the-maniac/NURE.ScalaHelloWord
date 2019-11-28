import akka.actor.Actor
import akka.actor.ActorSystem
import akka.actor.Props
import akka.actor.ActorRef
import akka.actor.Timers
import scala.concurrent.duration._

case object PingMessage
case object PongMessage
case object MyTickKey
case object MyTickPong
case object StartMessage
case object StopMessage

class Ping(pong: ActorRef) extends Actor with Timers {
    var count = 0
    var a : ActorRef = null
    def incrementAndPrint { count += 1; println("ping"); println("Message Number: " + count.toString); }
    def receive = {
        case StartMessage =>
            incrementAndPrint
            pong ! PingMessage
        case MyTickPong =>
            a ! PingMessage
        case PongMessage =>
            incrementAndPrint
            if (count > 99) {
                sender ! StopMessage
                println("ping stopped")
                context.stop(self)
            } else {
                a = sender()
                timers.startTimerWithFixedDelay(MyTickKey, MyTickPong, 500.milliseconds)
                println("waiting before sending ping message")
            }
        case _ => println("Ping got something unexpected.")
    }
}

class Pong extends Actor {
    def receive = {
      case PingMessage =>
          println(" pong")
          sender ! PongMessage
      case StopMessage =>
          println("pong stopped")
          context.stop(self)
      case _ => println("Pong got something unexpected.")
    }
}


object Main extends App {
  val system = ActorSystem("PingPongSystem")
  val pong = system.actorOf(Props[Pong], name = "pong")
  val ping = system.actorOf(Props(new Ping(pong)), name = "ping")
  ping ! StartMessage
  //system.shutdown
}