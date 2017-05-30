
//
import java.io.{BufferedReader, FileReader}

import com.webcohesion.ofx4j.io.OFXHandler
import com.webcohesion.ofx4j.io.nanoxml.NanoXMLOFXReader

val reader = new NanoXMLOFXReader()



val hand = new CustomHandler()
val bf = new BufferedReader(new FileReader("C:\\scala\\scala-budget\\ofx.ofx"))


reader.setContentHandler(hand)
//bf.lines().count()
val qwe = reader.parse(bf)


hand.aggregateStack
//println("ahoj")
//val filename = "C:\\work\\wallet-be\\test\\com\\budgetbakers\\be\\imports\\integration\\csv\\airbank.csv"
//val input = new FileInputStream(new File("invalid.txt"));
//import java.nio.charset.{Charset, CodingErrorAction}
//val decoder = Charset.forName("UTF-8").newDecoder()
//decoder.onMalformedInput(CodingErrorAction.IGNORE)
//val readerIS = new InputStreamReader(input, decoder)
//val bufferedReader = new BufferedReader(readerIS)
//var line = bufferedReader.readLine()
//while( line != null ) {
//  println(line)
//  line = bufferedReader.readLine()
//}
//bufferedReader.close()
//println("ahoj2")



class CustomHandler(types: List[String]) extends OFXHandler {

  var aggregateName: String = ""
  var aggregateValues: List[(String, Any)] = List.empty
  var parseOk: Boolean = false
  var remaining: List[String] = types

  override def onHeader(name: String, value: String): Unit = {}

  override def startAggregate(name: String): Unit = {
    if(!parseOk) {
      aggregateName = name
    }
  }

  override def onElement(name: String, value: String): Unit = {
    if(!parseOk) {
      aggregateValues
    }
    println(s"${List.fill(aggregateStack.size * 2)(" ").mkString} $name -> $value")
    //aggregateStack.last += (name -> value)

  }

  override def endAggregate(aggregateName: String): Unit = {
//    println(s"endAggregate: $aggregateName ${aggregateStack.size}")
    if(!parseOk) {

    }
    aggregateStack = aggregateStack.dropRight(1)
    println(s"${List.fill(aggregateStack.size * 2)(" ").mkString}}")
  }
}
