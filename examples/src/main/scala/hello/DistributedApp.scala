package hello

import com.vitthalmirji.valmuri.config.VConfig
import com.vitthalmirji.valmuri.core.VApplication
import com.vitthalmirji.valmuri.error.VResult
import com.vitthalmirji.valmuri.http.{ VRequest, VRoute }

import java.util.concurrent.ConcurrentHashMap
import scala.jdk.CollectionConverters._

case class Node(id: String, host: String, port: Int, lastSeen: Long)

object DistributedApp extends VApplication {

  private val nodes  = new ConcurrentHashMap[String, Node]()
  private val nodeId = java.util.UUID.randomUUID().toString

  override def configure(): VResult[Unit] = VResult.success(VConfig.load())

  override def routes() = List(
    // Node registration
    VRoute.safe("/nodes/register", handleNodeRegistration),

    // Node discovery
    VRoute.safe("/nodes", _ => VResult.success(listNodes())),

    // Health check for this node
    VRoute.safe("/health", _ => VResult.success(s"""{"nodeId":"$nodeId","status":"UP"}""")),

    // Work distribution endpoint
    VRoute.safe("/work/distribute", handleWorkDistribution),

    // Node status dashboard
    VRoute.safe("/", _ => VResult.success(renderDashboard())),

    // API for getting node-specific data
    VRoute.safe("/nodes/:id/status", handleNodeStatus)
  )

  private def handleNodeRegistration(request: VRequest): VResult[String] = {
    val node = Node(
      id = java.util.UUID.randomUUID().toString,
      host = request.headers.getOrElse("Host", "localhost"),
      port = config.serverPort,
      lastSeen = System.currentTimeMillis()
    )

    nodes.put(node.id, node)
    VResult.success(s"""{"nodeId":"${node.id}","status":"registered"}""")
  }

  private def listNodes(): String = {
    val nodesList = nodes.values().asScala.toList
    val nodesJson = nodesList
      .map(node => s"""{"id":"${node.id}","host":"${node.host}","port":${node.port},"lastSeen":${node.lastSeen}}""")
      .mkString(",")

    s"""{"nodes":[$nodesJson],"count":${nodesList.size}}"""
  }

  private def handleWorkDistribution(request: VRequest): VResult[String] = {
    val availableNodes = nodes
      .values()
      .asScala
      .filter(node => System.currentTimeMillis() - node.lastSeen < 30000 // 30 seconds timeout
      )
      .toList

    if (availableNodes.nonEmpty) {
      val selectedNode = availableNodes(scala.util.Random.nextInt(availableNodes.size))
      VResult.success(
        s"""{"assignedNode":"${selectedNode.id}","host":"${selectedNode.host}","port":${selectedNode.port}}"""
      )
    } else {
      VResult.success(s"""{"error":"No available nodes"}""")
    }
  }

  private def handleNodeStatus(request: VRequest): VResult[String] =
    request.params.get("id") match {
      case Some(nodeId) =>
        nodes.asScala.get(nodeId) match {
          case Some(node) =>
            val uptime = System.currentTimeMillis() - node.lastSeen
            VResult.success(s"""{"id":"${node.id}","uptime":${uptime},"status":"active"}""")
          case None =>
            VResult.success(s"""{"error":"Node not found"}""")
        }
      case None =>
        VResult.success(s"""{"error":"Node ID required"}""")
    }

  private def renderDashboard(): String = {
    val nodesList = nodes.values().asScala.toList
    val nodesHtml = nodesList
      .map(node => s"""
      <tr>
        <td>${node.id}</td>
        <td>${node.host}:${node.port}</td>
        <td>${(System.currentTimeMillis() - node.lastSeen) / 1000}s ago</td>
        <td><span class="status-active">Active</span></td>
      </tr>
      """)
      .mkString

    s"""
    <!DOCTYPE html>
    <html>
    <head>
        <title>Distributed App Dashboard</title>
        <style>
            body { font-family: Arial, sans-serif; margin: 40px; }
            .dashboard { max-width: 1200px; }
            .stats { display: flex; gap: 20px; margin-bottom: 30px; }
            .stat-card { background: #f5f5f5; padding: 20px; border-radius: 8px; flex: 1; }
            table { width: 100%; border-collapse: collapse; }
            th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
            .status-active { color: green; font-weight: bold; }
            .btn { background: #007cba; color: white; padding: 10px 20px; border: none; border-radius: 4px; cursor: pointer; }
        </style>
    </head>
    <body>
        <div class="dashboard">
            <h1>Valmuri Distributed Application</h1>

            <div class="stats">
                <div class="stat-card">
                    <h3>Total Nodes</h3>
                    <p style="font-size: 2em; margin: 0;">${nodesList.size}</p>
                </div>
                <div class="stat-card">
                    <h3>Current Node</h3>
                    <p>$nodeId</p>
                </div>
                <div class="stat-card">
                    <h3>Uptime</h3>
                    <p>${System.currentTimeMillis() / 1000}s</p>
                </div>
            </div>

            <h2>Active Nodes</h2>
            <table>
                <thead>
                    <tr>
                        <th>Node ID</th>
                        <th>Address</th>
                        <th>Last Seen</th>
                        <th>Status</th>
                    </tr>
                </thead>
                <tbody>
                    $nodesHtml
                </tbody>
            </table>

            <div style="margin-top: 30px;">
                <button class="btn" onclick="distributeWork()">Distribute Work</button>
                <button class="btn" onclick="location.reload()">Refresh</button>
            </div>
        </div>

        <script>
            function distributeWork() {
                fetch('/work/distribute', { method: 'POST' })
                    .then(response => response.json())
                    .then(data => alert('Work assigned to: ' + (data.assignedNode || 'No nodes available')));
            }

            // Auto-refresh every 10 seconds
            setInterval(() => location.reload(), 10000);
        </script>
    </body>
    </html>
    """
  }

  def main(args: Array[String]): Unit =
    start() match {
      case VResult.Success(_) =>
        println(s"Distributed app node $nodeId running on port ${config.serverPort}")
        Thread.currentThread().join()
      case VResult.Failure(error) =>
        println(s"Failed to start: ${error.message}")
    }
}
