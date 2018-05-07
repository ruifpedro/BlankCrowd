package pt.bc

import java.util

import scala.collection.JavaConverters._
import scala.collection.mutable

object Agent {
	var agentIps = Set.empty[String]

	def getAgentIps: util.Set[String] = agentIps.asJava

	def main(args: Array[String]): Unit = {
		val broadcasters = mutable.HashSet.empty[Thread]
		val listeners = mutable.HashSet.empty[Thread]


		val peerMonitor = new PeersMonitor

		System.out.println("Start ping")
		NetUtil.buildInterfaceList().foreach { case (name, list) =>
			list.foreach(tup => {
				val interfaceIp = tup._1.getHostAddress
				//skip localhost
				if(!interfaceIp.equals("127.0.0.1")){
					val netPrefix = tup._3
					val netMask = NetUtil.netPrefixToIP(netPrefix)
					NetUtil.pingNetwork(interfaceIp, netMask)
				}
			})
		}
		System.out.println("Ping finished")

		System.out.println("Start scan")
		NetUtil.scan().foreach { case (interfaceIp, arpIpList) =>
			arpIpList.foreach { l =>

				(!l._1.equals("255.255.255.255"), !l._2.equals("ff-ff-ff-ff-ff-ff")) match {
					case (true, true) =>

						agentIps += interfaceIp.asInstanceOf[String]
						val bt = new BroadcastThread("broadcaster@" + interfaceIp, interfaceIp, l._1, peerMonitor)
						val lt = new ListenerThread("listener@" + interfaceIp, interfaceIp, l._1)
						broadcasters.add(bt)
						listeners.add(lt)
					case (_, _) =>
				}
			}
		}

		broadcasters.foreach(_.start())
		listeners.foreach(_.start())

		while (broadcasters.nonEmpty && listeners.nonEmpty) {
			Thread.sleep(10000)
			val trans = broadcasters.toSet
			val res: Set[Seq[String]] = trans.map {
				case thread: BroadcastThread =>
					thread.peers.asScala.toSeq

				case _ =>
					Seq.empty[String]
			}

			val peersList = res.flatten

			println(s"peers list : $peersList")
			println(s"peers list (mon): ${peerMonitor.getSet}")
		}
	}
}