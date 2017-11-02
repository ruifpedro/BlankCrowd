package pt.bc

import java.net.{InetAddress, NetworkInterface}

import scala.collection.JavaConversions._
import scala.sys.process._

/**
  * Created by Force on 18/10/2017.
  */
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

				if (address != null && broadcast != null && netprefix != null)
					list ::= ((address, broadcast, netprefix))
			}

			if (name != null && list.nonEmpty) {
				interfacesMap += (name -> list)
			}
		}
		interfacesMap
	}


	def scan(): Map[String, List[(String, String)]] = {
		val IP_PAT = "(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})".r
		val MAC_PAT = "([a-f0-9]{1,2}-[a-f0-9]{1,2}-[a-f0-9]{1,2}-[a-f0-9]{1,2}-[a-f0-9]{1,2}-[a-f0-9]{1,2})".r

		val interfacesInfo = buildInterfaceList().values.flatten

		val pat = scala.collection.mutable.Map.empty[String, List[(String, String)]]

		interfacesInfo.foreach{ entry =>

			val cmd = "arp -a -N "+entry._1.getHostAddress !!

			val temp = (IP_PAT + " .+ " + MAC_PAT).r.findAllMatchIn(cmd).map(_.subgroups).map(x => (x.head, x.last)).toList
			if(temp.nonEmpty)
				pat.put(entry._1.getHostAddress, temp)
		}

		pat.toMap
	}

	def getInterfaces: Seq[String] = {
		scan().keys.toSeq
	}
}
