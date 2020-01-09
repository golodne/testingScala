import scala.annotation.tailrec

def sum(list: List[Int]): Int = list match {
    case Nil => 0
    case x::tail => x + sum(tail)
}

@tailrec
def sum2(list: List[Int],acc: Int = 0): Int = list match {
  case Nil => acc
  case x::tail => sum2(tail,acc + x)
}

var list = List(1,2,3,4,5,6,7)
sum(list)
sum2(list) //with tailrec optimization

