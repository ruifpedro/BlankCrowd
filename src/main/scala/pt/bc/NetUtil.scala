package pt.bc

import java.net.{InetAddress, NetworkInterface}
import java.util.concurrent.atomic.AtomicLong

import scala.collection.JavaConversions._
import scala.collection.immutable.BitSet
import scala.sys.process._


object NetUtil {

	def buildInterfaceList(): Map[String, List[(InetAddress, InetAddress, Short)]] = {
		var interfacesMap = Map[String, List[(InetAddress, InetAddress, Short)]]()

		var interfaces = NetworkInterface.getNetworkInterfaces

		while (interfaces.hasMoreElements) {
			val interface = interfaces.nextElement()

			val name = interface.getName

			var list = List[(InetAddress, InetAddress, Short)]()
			interface.getInterfaceAddresses.toList.foreach { addr =>
				val address = addr.getAddress
				val broadcast = addr.getBroadcast
				val netprefix = addr.getNetworkPrefixLength
				//				interface.getDisplayName
				//				address.getHostName.

				if (address != null && broadcast != null && netprefix != null)
					list ::= ((address, broadcast, netprefix))
			}

			if (name != null && list.nonEmpty) {
				interfacesMap += (name -> list)
			}
		}
		interfacesMap
	}


	def netPrefixToIP(p: Int): String = {
		var s = (0 until p).map(_ => "1").foldLeft("")(_ + _)

		(s.length until 32).foreach(_ => s += "0")

		binStringToIp(s)
	}

	def scan(): Map[String, List[(String, String)]] = {
		//todo - extract
		val osname = System.getProperty("os.name")
		OSUtil.parse(osname) match {
			case OSUtil.WindowsOS => windowsScan()
			case OSUtil.LinuxOS => linuxScan()
			case OSUtil.MacOS => Map.empty[String, List[(String, String)]]
			//todo - to be implemented
		}
	}


	private def windowsScan(): Map[String, List[(String, String)]] = {
		val IP_PAT = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})".r
		val MAC_PAT = "([a-f0-9]{1,2}-[a-f0-9]{1,2}-[a-f0-9]{1,2}-[a-f0-9]{1,2}-[a-f0-9]{1,2}-[a-f0-9]{1,2})".r

		val interfacesInfo = buildInterfaceList().values.flatten

		val pat = scala.collection.mutable.Map.empty[String, List[(String, String)]]

		interfacesInfo.foreach { entry =>

			val cmd = "arp -a -N " + entry._1.getHostAddress !!

			val temp = (IP_PAT + " .+ " + MAC_PAT).r.findAllMatchIn(cmd).map(_.subgroups).map(x => (x.head, x.last)).toList
			if (temp.nonEmpty)
				pat.put(entry._1.getHostAddress, temp)
		}

		pat.toMap
	}


	private def linuxScan(): Map[String, List[(String, String)]] = {
		val IP_PAT = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})".r
		val MAC_PAT = "([a-f0-9]{2}:[a-f0-9]{2}:[a-f0-9]{2}:[a-f0-9]{2}:[a-f0-9]{2}:[a-f0-9]{2})".r

		//todo - may not be enough
		val interfacesInfo = buildInterfaceList()

		val pat = scala.collection.mutable.Map.empty[String, List[(String, String)]]

		interfacesInfo.foreach { entry =>
			//todo - review

			val cmd = "arp -i " + entry._1 !!

			val temp = (IP_PAT + " .+ " + MAC_PAT).r.findAllMatchIn(cmd).map(_.subgroups).map(x => (x.head, x.last)).toList
			if (temp.nonEmpty)
				pat.put(interfacesInfo(entry._1).head._1.getHostAddress, temp)
		}

		pat.toMap
	}

	private def ifconfig(): List[String] = {
		val PAT = "([a-z0-9]+)\\s+Link\\sencap".r

		val cmd = "ifconfig" !!

		val temp = PAT.findAllMatchIn(cmd).map(_.subgroups).flatten.toList
		if (temp.nonEmpty)
			return temp
		List.empty[String]
	}

	def ping(ip: String): Unit = {
		val osname = System.getProperty("os.name")
		OSUtil.parse(osname) match {
			case OSUtil.WindowsOS =>
				pingWindows(ip)


			case OSUtil.LinuxOS =>
				pingLinux(ip)

			case OSUtil.MacOS =>
			//todo - to be implemented

		}
	}

	def pingNetwork(network: String, mask: String): Unit = {
		val IP_PAT = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})".r

		if (IP_PAT.findAllMatchIn(network).isEmpty && IP_PAT.findAllMatchIn(mask).isEmpty)
			return

		val ips = genIps(network, mask)

		val c = new AtomicLong(ips.size)

		//		val pingMany = Future {
		//			ips.par.foreach(ip => Await.result(ping(ip), Duration.Inf))
		ips.toList.sorted.foreach { ip =>
			val r = new Runnable {
				override def run(): Unit = {
					ping(ip)
					c.decrementAndGet()
				}
			}
			new Thread(r).start()
		}
		//		}

		//		Await.result(pingMany, Duration.Inf)
		//		pingMany
		var cc = c.get()
		do {
			Thread.sleep(100)
			cc = c.get()
		} while (cc > 0)
		println("ENDED")
	}

	private def pingWindows(ip: String): Unit = {
		val IP_PAT = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})".r

		if (IP_PAT.findAllMatchIn(ip).isEmpty)
			return
		println(ip)
		//forces arp requests
		try {
			"ping -n 3 " + ip !!
		} catch {
			case e: RuntimeException =>
		}
		println(ip + " Ended")
	}

	def genIps(network: String, mask: String): Set[String] = {
		var ips = Set.empty[String]

		def ipToBin(s: String) = s.split('.').transform(stringToBinary(_, 8)).reduce((ip1, ip2) => ip1 + ip2)


		val networkBinary = ipToBin(network)
		val maskBinary = ipToBin(mask)


		val netBinBit = BitSet.fromBitMask(networkBinary.map(v => v.toString.toLong).toArray)

		val maskBinBit = BitSet.fromBitMask(maskBinary.map(v => v.toString.toLong).toArray)


		val and = netBinBit & maskBinBit

		val cut = maskBinary.indexOf("0")
		val minMask = maskBinary.substring(cut)
		val maxMask = minMask.replace("0", "1")

		val maxMaskVal = binStringToNum(maxMask)


		(0 to maxMaskVal).foreach {
			v =>
				val nb = BitSet.fromBitMask(v.toBinaryString.map(v => v.toString.toLong).toArray)

				val bm = nb.toBitMask

				var end = bm.map(v2 => v2.toString).foldLeft("")(_ + _)

				if (end.length < 32 - cut) {
					(0 until 32 - cut - end.length).foreach(_ => end = "0" + end)
				}

				ips += binStringToIp(networkBinary.substring(0, cut) + end)
		}
		ips
	}

	def binStringToNum(s: String) = s.toList.reverse.map(c => c.toString.toLong).zipWithIndex.map {
		case (v: Long, i: Int) =>
			if (v == 1)
				Math.pow(2, i)
			else
				0
	}.sum.toInt


	def binStringToIp(s: String): String = {
		val c1 = s.substring(0, 8)
		val v1 = binStringToNum(c1)

		val c2 = s.substring(8, 16)
		val v2 = binStringToNum(c2)

		val c3 = s.substring(16, 24)
		val v3 = binStringToNum(c3)

		val c4 = s.substring(24, 32)
		val v4 = binStringToNum(c4)
		v1 + "." + v2 + "." + v3 + "." + v4
	}

	def stringToBinary(s: String, d: Int): String = {
		var binary = s.toInt.toBinaryString
		(1 to d - binary.length).foreach {

			_ =>
				binary = "0" + binary
		}
		binary
	}

	private def pingLinux(ip: String): Boolean = {
		val IP_PAT = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})".r

		if (IP_PAT.findAllMatchIn(ip).isEmpty)
			return false

		println(ip)
		//forces arp requests
		try {
			"ping -c 3 " + ip lineStream_!
		} catch {
			case e: RuntimeException =>
		}
		true
	}

	def getInterfaces: Seq[String] = {
		scan().keys.toSeq
	}
}
