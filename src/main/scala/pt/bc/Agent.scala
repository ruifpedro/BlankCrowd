package pt.bc

import scala.collection.mutable
import scala.collection.JavaConverters._

object Agent {
	def main(args: Array[String]): Unit = {
		val broadcasters = mutable.HashSet.empty[Thread]
		val listeners = mutable.HashSet.empty[Thread]

		var agentIps = Set.empty[String]

		NetUtil.scan().foreach { case (interfaceIp, arpIpList) =>
			arpIpList.foreach { l =>

				(!l._1.equals("255.255.255.255"), !l._2.equals("ff-ff-ff-ff-ff-ff")) match {
					case (true, true) => {

						agentIps += interfaceIp.asInstanceOf[String]
						val bt = new BroadcastThread("broadcaster", interfaceIp, l._1)
						val lt = new ListenerThread("listener", interfaceIp, l._1)
						broadcasters.add(bt)
						listeners.add(lt)
					}
					case (_, _) =>
				}
			}
		}

		broadcasters.foreach(_.start())
		listeners.foreach(_.start())

		while (broadcasters.nonEmpty && listeners.nonEmpty) {
			Thread.sleep(10000)
			val trans = broadcasters.toSet
			val res: Set[Seq[String]] = trans.map(t => {
				t match {
					case thread: BroadcastThread =>
						thread.peers.asScala.toSeq

					case _ =>
						Seq.empty[String]
				}
			})

			val peersList = res.flatten

			println(s"peers list : $peersList")
		}
	}
}

class Agent{
	println("started")
}