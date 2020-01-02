package future
import future.FutureServer.Event
import spray.json.DefaultJsonProtocol
object JsonFormats {
  import DefaultJsonProtocol._
  implicit val eventJsonFormat = jsonFormat2(Event)
}
