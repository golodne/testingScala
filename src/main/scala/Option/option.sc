//good article https://danielwestheide.com/blog/the-neophytes-guide-to-scala-part-5-the-option-type/

val greeting: Option[String] = Some("Hello world")

val absentGreeting: Option[String] = Option(null)

case class User (
  id: Int,
  firstName: String,
  lastName: String,
  age: Int,
  gender: Option[String]
)

object UserRepository {
  private val users = Map(1 -> User(1,"John","Doe",32, Some("male")),

    2 -> User(1,"Johanna","Doe",30, None),
    4 -> User(1,"Vladimir","Goncharov",35, Some("female"))

  )

  def findById(id: Int): Option[User] = users.get(id)
  def findAll = users.values
}

val user1 = UserRepository.findById(1)
if (user1.isDefined) {
  println(user1.get.firstName)
}

val user2 = User(1,"Johanna","Doe",30, None)
    println(user2.gender.getOrElse("not specified"))

   user2.gender match {
     case Some(g) => println(s"user2 has gender $g")
     case None => println("not specified")
   }

UserRepository.findById(1).foreach{ user => println(user.firstName) }

//mapping an option
val findage = UserRepository.findById(1).map(_.age)
    println(findage.getOrElse("not data"))

//flatmap and options Option[Option[gander]]
val findgander = UserRepository.findById(2).map(_.gender) //Some(None)

val findgander2 = UserRepository.findById(2).flatMap(_.gender) //None

val names: List[Option[String]] = List(Some("vladimir"),None,Some("petr"),None)
val t1 = names.map(x => x.map(_.toUpperCase))//.flatten
val t2 = names.flatMap(x => x.map(_.toUpperCase))

//filtering an option
UserRepository.findById(1).filter(_.age > 30) //Some(User(1,John,Doe,32,Some(male)))
UserRepository.findById(1).filter(_.age < 30) //None becouse age < 30 not found
UserRepository.findById(3).filter(_.age > 30) //None becouse userid = 3 is not found

//For comprehensions
val findGender = for {
  user <- UserRepository.findById(1)
  gender <- user.gender
} yield gender //findGender: Option[String] = Some(male)

val listGenders = for {
  users <- UserRepository.findAll
  genders <- users.gender
} yield genders //listGenders: Iterable[String] = List(male, female)

//Usage in the left side of a generator
for {
  User(_,_,_,_,Some(gender)) <- UserRepository.findById(10)
} yield gender //res8: Option[String] = Some(male) or None

//Chaining options
val A: Option[String] = Some("resource1")
val B: Option[String] = None
val C = A.orElse(B) //Option[String] = Some(resource1)


















