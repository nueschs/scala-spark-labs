package com
package scigility
package day3tooling


import cats.implicits._
import cats.effect.{ExitCode, IO, IOApp, Effect}
import com.scigility.day3.Program.{Message, Response, encM, decR}


object Main extends IOApp {

  final case class Expect(msg:Message, expect: Response => Boolean, should:String)

  sealed trait TestChain
  final case object TestEnd extends TestChain
  final case class TestCons(expect:Expect, next:TestChain) extends TestChain

  object TestChain{
    def test[F[_]: Effect](alg:AsyncKafkaAlgebra[F])(chain:TestChain):F[Option[Expect]] = chain match {
      case TestEnd => Effect[F].pure(None)
      case TestCons(expect, next) => alg.sendAndAwaitResponse[String, Message, Response](_.msgKey, _.msgKey)(expect.msg)
          .flatMap(
            response => if (expect.expect(response)) test(alg)(next) else Effect[F].pure(Some(expect))
          )
    }
  }

  type MessageChain = List[Expect]

  def testAll[F[_]: Effect](alg:AsyncKafkaAlgebra[F])(chains:List[TestChain]):F[List[Expect]] = chains.traverse(TestChain.test(alg)).map(_.collect{ case Some(x) => x})

  override def run(args: List[String]): IO[ExitCode] = AsyncKafkaAlgebra.kafkaAlg[IO](
    List("broker"), 
    "groupId", 
    "inputTopic", 
    "outputTopic"
  ).use(
    alg => testAll(alg)(
      List()
    )
  ).flatMap(
    errs => IO {
      println("Failed Cases:")
      errs.map( x => s"MSG: ${x.msg} Explanation: ${x.should}").foreach(println)
    }
  ).map(_ => ExitCode.Success)
}
