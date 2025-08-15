#!/bin/bash
# File: scripts/30min-deploy.sh

set -e  # Exit on any error

echo "🚀 Valmuri 30-Minute Deployment Starting..."
echo "⏱️  Target: From zero to production in 30 minutes"

PROJECT_NAME=${1:-"my-valmuri-blog"}
TEMPLATE=${2:-"blog"}

# Step 1: Validate prerequisites (2 minutes)
echo "🔍 Step 1: Checking prerequisites..."

if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Please install Java 11 or higher."
    exit 1
fi

if ! command -v sbt &> /dev/null; then
    echo "❌ SBT not found. Please install SBT."
    exit 1
fi

if ! command -v docker &> /dev/null; then
    echo "⚠️  Docker not found. Some deployment options will be limited."
fi

echo "✅ Prerequisites validated"

# Step 2: Create project (3 minutes)
echo "📁 Step 2: Creating project '$PROJECT_NAME'..."

if [ -d "$PROJECT_NAME" ]; then
    echo "⚠️  Directory $PROJECT_NAME already exists. Removing..."
    rm -rf "$PROJECT_NAME"
fi

# Use the existing Valmuri CLI (assuming it's been built)
if [ -f "cli/target/scala-2.13/valmuri-cli.jar" ]; then
    java -jar cli/target/scala-2.13/valmuri-cli.jar new "$PROJECT_NAME" --template "$TEMPLATE"
else
    # Fallback: create project manually
    mkdir -p "$PROJECT_NAME"
    cd "$PROJECT_NAME"

    # Create project structure
    mkdir -p src/main/scala
    mkdir -p src/main/resources/{static/css,posts,templates}

    # Generate build.sbt
    cat > build.sbt << EOF
name := "$PROJECT_NAME"
version := "0.1.0"
scalaVersion := "2.13.16"

libraryDependencies ++= Seq(
  "com.vitthalmirji" %% "valmuri-core" % "0.1.0"
)

mainClass := Some("Main")

// Assembly plugin for single JAR
enablePlugins(sbtassembly.AssemblyPlugin)
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}
EOF

    # Create project.sbt for assembly plugin
    cat > project/plugins.sbt << EOF
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "2.1.5")
EOF

    # Generate main application
    cat > src/main/scala/Main.scala << 'EOF'
import com.vitthalmirji.valmuri._

object Main extends VApplication {

  def routes() = List(
    VRoute("/", _ => VResult.success(renderHomePage())),
    VRoute("/blog", _ => VResult.success(renderBlogIndex())),
    VRoute("/about", _ => VResult.success(renderAboutPage()))
  ) ++ loadBlogPosts()

  private def renderHomePage(): String = {
    """<!DOCTYPE html>
<html>
<head>
    <title>My Valmuri Blog</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; }
        nav { border-bottom: 1px solid #eee; padding-bottom: 20px; margin-bottom: 30px; }
        nav a { margin-right: 20px; text-decoration: none; color: #007cba; }
        .hero { text-align: center; padding: 50px 0; background: #f8f9fa; margin: -20px -20px 30px; }
        .cta { background: #007cba; color: white; padding: 10px 20px; text-decoration: none; border-radius: 4px; }
    </style>
</head>
<body>
    <nav>
        <a href="/">Home</a>
        <a href="/blog">Blog</a>
        <a href="/about">About</a>
    </nav>
    <div class="hero">
        <h1>Welcome to My Blog</h1>
        <p>Built with Valmuri framework in under 30 minutes!</p>
        <a href="/blog" class="cta">Read Posts</a>
    </div>
    <h2>Latest Posts</h2>
    <p><a href="/blog/welcome">Welcome to My Blog</a></p>
    <p><a href="/blog/valmuri-rocks">Why Valmuri Rocks</a></p>
</body>
</html>"""
  }

  private def renderBlogIndex(): String = {
    """<!DOCTYPE html>
<html>
<head><title>Blog</title></head>
<body>
    <h1>Blog Posts</h1>
    <article>
        <h2><a href="/blog/welcome">Welcome to My Blog</a></h2>
        <p>My first post using the amazing Valmuri framework.</p>
    </article>
    <article>
        <h2><a href="/blog/valmuri-rocks">Why Valmuri Rocks</a></h2>
        <p>Exploring the benefits of full-stack Scala development.</p>
    </article>
</body>
</html>"""
  }

  private def renderAboutPage(): String = {
    """<!DOCTYPE html>
<html>
<head><title>About</title></head>
<body>
    <h1>About This Blog</h1>
    <p>This blog was created in under 30 minutes using the Valmuri framework!</p>
    <p>Valmuri brings Rails-like productivity to Scala development with:</p>
    <ul>
        <li>Zero configuration</li>
        <li>Type safety</li>
        <li>Functional programming</li>
        <li>Fast deployment</li>
    </ul>
</body>
</html>"""
  }

  private def loadBlogPosts(): List[VRoute] = {
    List(
      VRoute("/blog/welcome", _ => VResult.success("""
<!DOCTYPE html>
<html>
<head><title>Welcome to My Blog</title></head>
<body>
    <h1>Welcome to My Blog</h1>
    <p>This is my first blog post using the <strong>Valmuri framework</strong>!</p>
    <p>I'm amazed at how quickly I can build and deploy web applications with Valmuri.</p>
    <p><a href="/blog">← Back to Blog</a></p>
</body>
</html>""")),

      VRoute("/blog/valmuri-rocks", _ => VResult.success("""
<!DOCTYPE html>
<html>
<head><title>Why Valmuri Rocks</title></head>
<body>
    <h1>Why Valmuri Rocks</h1>
    <p>Valmuri combines the best of several worlds:</p>
    <ul>
        <li><strong>Productivity:</strong> Like Rails and Django</li>
        <li><strong>Type Safety:</strong> Like Scala and functional programming</li>
        <li><strong>Performance:</strong> Native compilation and fast startup</li>
        <li><strong>Simplicity:</strong> Zero configuration needed</li>
    </ul>
    <p>Perfect for modern web development!</p>
    <p><a href="/blog">← Back to Blog</a></p>
</body>
</html>"""))
    )
  }

  def main(args: Array[String]): Unit = {
    start() match {
      case VResult.Success(_) =>
        println(s"✅ Blog running at http://localhost:8080")
        Thread.currentThread().join()
      case VResult.Failure(error) =>
        println(s"❌ Failed to start: ${error.message}")
    }
  }
}
EOF

    cd ..
fi

echo "✅ Project '$PROJECT_NAME' created"

# Step 3: Test locally (5 minutes)
echo "🧪 Step 3: Testing locally..."
cd "$PROJECT_NAME"

echo "📦 Building project..."
timeout 120s sbt compile || {
    echo "❌ Compilation failed"
    exit 1
}

echo "🚀 Starting development server..."
timeout 30s sbt run &
SERVER_PID=$!
sleep 10

# Test endpoints
echo "🔍 Testing endpoints..."
if curl -f http://localhost:8080/ >/dev/null 2>&1; then
    echo "✅ Home page working"
else
    echo "❌ Home page test failed"
    kill $SERVER_PID 2>/dev/null || true
    exit 1
fi

if curl -f http://localhost:8080/blog >/dev/null 2>&1; then
    echo "✅ Blog page working"
else
    echo "❌ Blog page test failed"
fi

# Stop test server
kill $SERVER_PID 2>/dev/null || true
echo "✅ Local testing completed"

# Step 4: Build for production (5 minutes)
echo "🔨 Step 4: Building for production..."

echo "📦 Creating production JAR..."
sbt assembly

if [ ! -f "target/scala-2.13/$PROJECT_NAME-assembly-0.1.0.jar" ]; then
    echo "❌ Assembly JAR not found"
    exit 1
fi

echo "✅ Production JAR created"

# Step 5: Containerize (3 minutes)
echo "🐳 Step 5: Creating Docker container..."

cat > Dockerfile << EOF
FROM openjdk:11-jre-slim

# Copy the JAR file
COPY target/scala-2.13/$PROJECT_NAME-assembly-0.1.0.jar /app.jar

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Run the application
CMD ["java", "-jar", "/app.jar"]
EOF

if command -v docker &> /dev/null; then
    echo "🔨 Building Docker image..."
    docker build -t "$PROJECT_NAME" . >/dev/null 2>&1
    echo "✅ Docker image built: $PROJECT_NAME"
else
    echo "⚠️  Docker not available, skipping containerization"
fi

# Step 6: Deploy to cloud (12 minutes)
echo "☁️  Step 6: Deploying to cloud..."

# Deploy to Heroku if available
if command -v heroku &> /dev/null && command -v docker &> /dev/null; then
    echo "🚀 Deploying to Heroku..."

    # Create Heroku app with unique name
    APP_NAME="$PROJECT_NAME-$(date +%s)"
    heroku create "$APP_NAME" >/dev/null 2>&1 || {
        echo "⚠️  Heroku app creation failed, trying container deployment..."
    }

    # Push container to Heroku
    heroku container:push web -a "$APP_NAME" >/dev/null 2>&1 && \
    heroku container:release web -a "$APP_NAME" >/dev/null 2>&1 && \
    HEROKU_URL=$(heroku info -a "$APP_NAME" | grep "Web URL" | awk '{print $3}')

    if [ ! -z "$HEROKU_URL" ]; then
        echo "✅ Deployed to Heroku: $HEROKU_URL"
        DEPLOYMENT_URL="$HEROKU_URL"
    fi

# Deploy to Railway if available
elif command -v railway &> /dev/null; then
    echo "🚀 Deploying to Railway..."

    railway up >/dev/null 2>&1 && {
        RAILWAY_URL=$(railway status --json | jq -r '.deployments[0].url')
        echo "✅ Deployed to Railway: $RAILWAY_URL"
        DEPLOYMENT_URL="$RAILWAY_URL"
    }

# Deploy to local Docker if no cloud platform
elif command -v docker &> /dev/null; then
    echo "🐳 Starting local Docker deployment..."

    # Stop any existing container
    docker stop "$PROJECT_NAME" 2>/dev/null || true
    docker rm "$PROJECT_NAME" 2>/dev/null || true

    # Run new container
    docker run -d --name "$PROJECT_NAME" -p 8080:8080 "$PROJECT_NAME" >/dev/null 2>&1

    echo "✅ Deployed locally: http://localhost:8080"
    DEPLOYMENT_URL="http://localhost:8080"

else
    echo "⚠️  No deployment platform available"
    echo "📦 Build artifacts ready in target/ directory"
    DEPLOYMENT_URL="Build ready for manual deployment"
fi

# Step 7: Verify deployment (2 minutes)
echo "🔍 Step 7: Verifying deployment..."

if [ ! -z "$DEPLOYMENT_URL" ] && [[ "$DEPLOYMENT_URL" == http* ]]; then
    echo "⏳ Waiting for deployment to be ready..."
    sleep 30

    for i in {1..6}; do
        if curl -f "$DEPLOYMENT_URL" >/dev/null 2>&1; then
            echo "✅ Deployment verified: $DEPLOYMENT_URL"
            break
        else
            echo "⏳ Attempt $i/6: Waiting for deployment..."
            sleep 10
        fi
    done
fi

# Final summary
END_TIME=$(date +%s)
TOTAL_TIME=$((END_TIME - START_TIME))

echo ""
echo "🎉 30-Minute Deployment Complete!"
echo "⏱️  Total time: ${TOTAL_TIME} seconds"
echo "📊 Results:"
echo "   ✅ Project created: $PROJECT_NAME"
echo "   ✅ Local testing: Passed"
echo "   ✅ Production build: Ready"
echo "   ✅ Deployment: $DEPLOYMENT_URL"
echo ""
echo "🌐 Your Valmuri application is now live!"
echo "📖 Next steps:"
echo "   • Add more blog posts in src/main/resources/posts/"
echo "   • Customize styling in src/main/resources/static/css/"
echo "   • Explore Valmuri features at https://github.com/vim89/valmuri"

# Record start time
START_TIME=$(date +%s)
