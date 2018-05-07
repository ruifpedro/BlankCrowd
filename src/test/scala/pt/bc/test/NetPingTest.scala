package pt.bc.test

import pt.bc.NetUtil

/**
  * Created by Force on 07/12/2017.
  */
object NetPingTest {
	def main(args: Array[String]): Unit = {
		val s1 = "11000000101010000000000100000001"

		val s2 = "00000000000000000000000000000000"



////		val ss1 = s1.toLong
////
////		val ss2 = s1.toLong
//
//		val b1 : mutable.BitSet = new mutable.BitSet()
//		s1.foreach(b1.add(_))
//
//
//		var b2 : mutable.BitSet = new mutable.BitSet()
//		s2.foreach(b2.add(_))
//
//
//
//		val r = b1 | b2
//		println(r)


//
		println(NetUtil.genIps("192.168.1.1", "255.255.255.0"))
//		println(NetUtil.genIps("192.168.1.1", "255.255.255.255").head.length)
//		println(NetUtil.genIps("255.255.255.255", "255.255.255.255"))
//		println(NetUtil.genIps("255.255.255.255", "255.255.255.255").head.length)
	}
}
