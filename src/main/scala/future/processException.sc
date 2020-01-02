import scala.concurrent._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

val fres = Future { throw new Exception("error!")}

//Результат Nothing потому что исключение в другом потоке
//    fres.foreach(value => println(value))

//val res = Await.result(fres,5 seconds)


/*
    fres.onComplete {
        case Failure(e) => println(e)
        case Success(value) => println(value)
    }
*/
/*
    //обработка только ошибки
    fres.onFailure {
        case e => println(e)
    }
*/
println(s"end $res")


