package pt.bc

object NetLinuxTest {
	def main(args: Array[String]): Unit = {
		val interfacesInfo = NetUtil.buildInterfaceList()

		println(interfacesInfo)

		println(NetUtil.scan())

	}
}
