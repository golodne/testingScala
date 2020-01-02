package future
import future.FutureServer.{Event, Route}
import spray.json.DefaultJsonProtocol
object JsonFormats {
  import DefaultJsonProtocol._
  implicit val eventJsonFormat = jsonFormat2(Event)
  implicit val routeJsonFormat = jsonFormat1(Route)
}
