import akka.actor.{ActorRef, Props}
import blocks.BDBlock
import nodeViewHolder._
import scorex.core.api.http.ApiRoute
import scorex.core.app.Application
import scorex.core.network.NodeViewSynchronizer
import scorex.core.network.message.MessageSpec
import scorex.core.settings.ScorexSettings
import transaction.{BDTransaction, Sha256PreimageProposition}

import scala.language.postfixOps

class BDApp(args: Seq[String]) extends {
  override implicit val settings: ScorexSettings = ScorexSettings.read(None)
} with Application {
  override type P = Sha256PreimageProposition
  override type TX = BDTransaction
  override type PMOD = BDBlock
  override type NVHT = BDNodeViewHolder

  override val apiRoutes: Seq[ApiRoute] = Seq.empty
  override protected val additionalMessageSpecs: Seq[MessageSpec[_]] = Seq.empty

  override val nodeViewHolderRef: ActorRef = BDNodeViewHolderRef(settings, timeProvider)

  override val nodeViewSynchronizer: ActorRef =
    actorSystem.actorOf(Props(new NodeViewSynchronizer[P, TX, BDSyncInfo, BDSyncInfoMessageSpec.type, PMOD, BDBlockchain, BDMempool](
      networkControllerRef, nodeViewHolderRef, localInterface, BDSyncInfoMessageSpec, settings.network, timeProvider)))


  override val localInterface: ActorRef = BDLocalInterfaceRef(nodeViewHolderRef)
  override val swaggerConfig: String = ""
}


object BDApp {

  def main(args: Array[String]): Unit = new BDApp(args).run()
}