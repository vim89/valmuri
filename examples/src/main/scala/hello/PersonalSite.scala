package hello

import com.vitthalmirji.valmuri._

object PersonalSite extends VApplication {

  override def configure(): VResult[Unit] = {
    // Register any custom services
    VResult.success(())
  }

  def routes(): List[VRoute] = List(
    // Home page
    VRoute("/", _ => VResult.success(renderHomePage())),

    // About page  
    VRoute("/about", _ => VResult.success(renderAboutPage())),

    // Projects showcase
    VRoute("/projects", _ => VResult.success(renderProjectsPage())),

    // Contact form
    VRoute("/contact", handleContactPage),
    VRoute.simple("/contact-submit", handleContactSubmit),

    // Resume download
    VRoute("/resume.pdf", _ => VResult.success("Resume content here")),

    // Blog section
    VRoute("/blog", _ => VResult.success(renderBlogIndex())),
    VRoute("/blog/valmuri-framework", _ => VResult.success(renderValmuriBlogPost())),
    VRoute("/blog/scala-web-development", _ => VResult.success(renderScalaBlogPost()))
  )

  private def renderHomePage(): String = {
    """<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Vitthal Mirji - Staff Data Engineer & Software Architect</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            line-height: 1.6;
            color: #333;
        }
        .container { max-width: 1200px; margin: 0 auto; padding: 0 20px; }
        
        /* Header */
        header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 100px 0;
            text-align: center;
        }
        .hero h1 { font-size: 3em; margin-bottom: 20px; }
        .hero p { font-size: 1.2em; margin-bottom: 10px; }
        .cta-button {
            display: inline-block;
            background: #ff6b6b;
            color: white;
            padding: 15px 30px;
            text-decoration: none;
            border-radius: 5px;
            margin-top: 30px;
            transition: transform 0.3s;
        }
        .cta-button:hover { transform: translateY(-2px); }
        
        /* Navigation */
        nav {
            background: white;
            padding: 15px 0;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            position: sticky;
            top: 0;
            z-index: 100;
        }
        nav ul {
            list-style: none;
            display: flex;
            justify-content: center;
        }
        nav li { margin: 0 20px; }
        nav a {
            text-decoration: none;
            color: #333;
            font-weight: 500;
            transition: color 0.3s;
        }
        nav a:hover { color: #667eea; }
        
        /* Highlights Section */
        .highlights {
            padding: 80px 0;
            background: #f8f9fa;
        }
        .highlights-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 40px;
            margin-top: 50px;
        }
        .highlight {
            background: white;
            padding: 40px;
            border-radius: 10px;
            text-align: center;
            box-shadow: 0 5px 20px rgba(0,0,0,0.1);
            transition: transform 0.3s;
        }
        .highlight:hover { transform: translateY(-5px); }
        .highlight h3 {
            color: #667eea;
            margin-bottom: 15px;
            font-size: 1.5em;
        }
        
        /* Tech Stack */
        .tech-stack {
            padding: 80px 0;
            text-align: center;
        }
        .tech-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
            gap: 30px;
            margin-top: 40px;
        }
        .tech-item {
            padding: 20px;
            background: #667eea;
            color: white;
            border-radius: 10px;
            font-weight: 500;
        }
        
        /* Footer */
        footer {
            background: #333;
            color: white;
            text-align: center;
            padding: 40px 0;
        }
        .social-links a {
            color: white;
            margin: 0 15px;
            text-decoration: none;
        }
        
        @media (max-width: 768px) {
            .hero h1 { font-size: 2em; }
            .highlights-grid { grid-template-columns: 1fr; }
        }
    </style>
</head>
<body>
    <header>
        <div class="container">
            <div class="hero">
                <h1>Vitthal Mirji</h1>
                <p>Staff Data Engineer & Software Architect</p>
                <p>Engineering the future of Data-Driven APIs, SDKs & AI solutions</p>
                <p>12+ years experience • Mumbai, India</p>
                <a href="/resume.pdf" class="cta-button">Download Resume</a>
            </div>
        </div>
    </header>
    
    <nav>
        <div class="container">
            <ul>
                <li><a href="/">Home</a></li>
                <li><a href="/about">About</a></li>
                <li><a href="/projects">Projects</a></li>
                <li><a href="/blog">Blog</a></li>
                <li><a href="/contact">Contact</a></li>
            </ul>
        </div>
    </nav>
    
    <section class="highlights">
        <div class="container">
            <h2 style="text-align: center; font-size: 2.5em; margin-bottom: 20px;">Expertise & Impact</h2>
            <div class="highlights-grid">
                <div class="highlight">
                    <h3>🏗️ Software Architecture</h3>
                    <p>Designing scalable systems for Fortune 500 companies with modern architectural patterns and best practices.</p>
                </div>
                <div class="highlight">
                    <h3>📊 Data Engineering</h3>
                    <p>Building robust data pipelines and analytics platforms that drive business decision-making.</p>
                </div>
                <div class="highlight">
                    <h3>🚀 Valmuri Framework</h3>
                    <p>Creator of the Valmuri full-stack Scala framework - bringing Rails productivity to functional programming.</p>
                </div>
                <div class="highlight">
                    <h3>🧠 Machine Learning</h3>
                    <p>Implementing AI-driven solutions and ML models for real-world business applications.</p>
                </div>
                <div class="highlight">
                    <h3>👥 Team Leadership</h3>
                    <p>Leading engineering teams and mentoring talent across diverse technical initiatives.</p>
                </div>
                <div class="highlight">
                    <h3>🌐 Open Source</h3>
                    <p>Active contributor to the open source ecosystem with focus on developer productivity tools.</p>
                </div>
            </div>
        </div>
    </section>
    
    <section class="tech-stack">
        <div class="container">
            <h2 style="font-size: 2.5em; margin-bottom: 20px;">Technology Stack</h2>
            <div class="tech-grid">
                <div class="tech-item">Scala</div>
                <div class="tech-item">Python</div>
                <div class="tech-item">Java</div>
                <div class="tech-item">Apache Spark</div>
                <div class="tech-item">Kafka</div>
                <div class="tech-item">ZIO</div>
                <div class="tech-item">Machine Learning</div>
                <div class="tech-item">AWS</div>
                <div class="tech-item">Docker</div>
                <div class="tech-item">Kubernetes</div>
            </div>
        </div>
    </section>
    
    <footer>
        <div class="container">
            <p>&copy; 2025 Vitthal Mirji. Built with Valmuri Framework.</p>
            <div class="social-links">
                <a href="https://linkedin.com/in/vitthalmirji">LinkedIn</a>
                <a href="https://github.com/vim89">GitHub</a>
                <a href="mailto:contact@vitthalmirji.com">Email</a>
            </div>
        </div>
    </footer>
</body>
</html>"""
  }

  private def renderAboutPage(): String = {
    """<!DOCTYPE html>
<html>
<head>
    <title>About - Vitthal Mirji</title>
    <link rel="stylesheet" href="/static/css/style.css">
</head>
<body>
    <h1>About Me</h1>
    
    <div class="about-content">
        <h2>Professional Journey</h2>
        <p>I'm a Computer Science Engineer and Staff Data Engineer from Mumbai with over 12 years of experience architecting innovative solutions for Fortune 500 companies. My passion lies in bridging the worlds of Big Data, AI, and software design to unlock technology's full potential.</p>
        
        <h2>Technical Expertise</h2>
        <ul>
            <li><strong>Data Engineering:</strong> Scalable data pipelines, real-time processing, analytics platforms</li>
            <li><strong>Software Architecture:</strong> Microservices, distributed systems, design patterns</li>
            <li><strong>Machine Learning:</strong> AI-driven solutions, predictive modeling, MLOps</li>
            <li><strong>Programming:</strong> Scala, Python, Java, functional programming</li>
            <li><strong>Big Data:</strong> Apache Spark, Kafka, Hadoop ecosystem</li>
            <li><strong>Cloud Platforms:</strong> AWS, Azure, containerization, Kubernetes</li>
        </ul>
        
        <h2>The Valmuri Story</h2>
        <p>After years of working with various web frameworks, I recognized a gap in the Scala ecosystem. While Django, Rails, and Spring Boot provide excellent developer experiences in their respective languages, Scala lacked a truly integrated framework that combined productivity with functional programming benefits.</p>
        
        <p>This inspired me to create <strong>Valmuri</strong> - a full-stack Scala framework that brings Rails-level productivity to functional programming, complete with type safety and modern architectural patterns.</p>
        
        <h2>Beyond Code</h2>
        <p>When I'm not architecting systems or writing code, you'll find me:</p>
        <ul>
            <li>🍳 <strong>Cooking:</strong> Exploring Maharashtrian cuisine and perfecting traditional recipes</li>
            <li>⌚ <strong>Watch Collecting:</strong> Curating a collection of timepieces with appreciation for craftsmanship</li>
            <li>🚗 <strong>Long Drives:</strong> Exploring the beautiful routes around Mumbai, Pune, and Goa</li>
            <li>🥃 <strong>Whisky Tasting:</strong> Developing palate for single malts and understanding terroir</li>
            <li>🎾 <strong>Tennis:</strong> Staying active on the courts around Mumbai</li>
            <li>📈 <strong>Investing:</strong> Analyzing market trends and building long-term wealth strategies</li>
            <li>🎵 <strong>Music:</strong> Enjoying Hindustani classical, Natya Sangeet, and Bollywood classics</li>
        </ul>
        
        <h2>Philosophy</h2>
        <p>I believe in the power of <em>jugaad</em> - the art of innovative problem-solving with available resources. This philosophy drives my approach to both technology and life, finding elegant solutions to complex challenges while maintaining a grounded, practical perspective.</p>
        
        <h2>Let's Connect</h2>
        <p>I'm always excited to discuss technology, share knowledge, and explore collaboration opportunities. Whether you're interested in data engineering, Scala development, or just want to chat about the latest in tech, feel free to reach out!</p>
    </div>
</body>
</html>"""
  }

  private def renderProjectsPage(): String = {
    """<!DOCTYPE html>
<html>
<head><title>Projects - Vitthal Mirji</title></head>
<body>
    <h1>Featured Projects</h1>
    
    <div class="project">
        <h2>🚀 Valmuri Framework</h2>
        <p><strong>Full-stack Scala web framework</strong></p>
        <p>A modern web framework that brings Django/Rails productivity to Scala functional programming. Features include auto-configuration, type-safe routing, dependency injection, and 30-minute deployment.</p>
        <div class="tech-tags">
            <span>Scala</span><span>HTTP</span><span>Functional Programming</span><span>Web Framework</span>
        </div>
        <div class="project-links">
            <a href="https://github.com/vim89/valmuri">GitHub</a>
            <a href="/blog/valmuri-framework">Blog Post</a>
        </div>
    </div>
    
    <div class="project">
        <h2>📊 Real-time Analytics Platform</h2>
        <p><strong>Enterprise data processing system</strong></p>
        <p>Scalable real-time analytics platform processing millions of events daily for Fortune 500 client. Includes ML-driven insights, predictive modeling, and interactive dashboards.</p>
        <div class="tech-tags">
            <span>Apache Spark</span><span>Kafka</span><span>Machine Learning</span><span>Scala</span>
        </div>
    </div>
    
    <div class="project">
        <h2>🏗️ Microservices Architecture</h2>
        <p><strong>Distributed system design</strong></p>
        <p>Designed and implemented microservices architecture for large-scale e-commerce platform. Improved system reliability, scalability, and development team velocity.</p>
        <div class="tech-tags">
            <span>Microservices</span><span>Docker</span><span>Kubernetes</span><span>API Design</span>
        </div>
    </div>
    
    <div class="project">
        <h2>🤖 ML-Powered Recommendation Engine</h2>
        <p><strong>AI-driven personalization system</strong></p>
        <p>Built recommendation engine using collaborative filtering and deep learning techniques. Improved user engagement by 40% and conversion rates by 25%.</p>
        <div class="tech-tags">
            <span>Machine Learning</span><span>Python</span><span>TensorFlow</span><span>MLOps</span>
        </div>
    </div>
    
    <div class="project">
        <h2>🔧 Developer Productivity Tools</h2>
        <p><strong>Internal tooling and automation</strong></p>
        <p>Created suite of developer tools including code generators, deployment automation, and monitoring dashboards. Reduced deployment time by 80% and onboarding time for new developers.</p>
        <div class="tech-tags">
            <span>DevOps</span><span>Automation</span><span>CLI Tools</span><span>CI/CD</span>
        </div>
    </div>
    
    <style>
        .project {
            background: #f8f9fa;
            padding: 30px;
            margin: 30px 0;
            border-radius: 10px;
            border-left: 5px solid #667eea;
        }
        .tech-tags {
            margin: 15px 0;
        }
        .tech-tags span {
            background: #667eea;
            color: white;
            padding: 5px 12px;
            border-radius: 15px;
            font-size: 0.9em;
            margin-right: 10px;
            display: inline-block;
            margin-bottom: 5px;
        }
        .project-links a {
            background: #28a745;
            color: white;
            padding: 8px 16px;
            text-decoration: none;
            border-radius: 4px;
            margin-right: 10px;
        }
    </style>
</body>
</html>"""
  }

  private def handleContactPage(request: VRequest): VResult[String] = {
    request.method match {
      case HttpMethod.GET => VResult.success(renderContactForm())
      case HttpMethod.POST => handleContactSubmit(request)
      case _ => VResult.failure(FrameworkError.RoutingError("Method not allowed"))
    }
  }

  private def renderContactForm(): String = {
    """<!DOCTYPE html>
<html>
<head><title>Contact - Vitthal Mirji</title></head>
<body>
    <h1>Get In Touch</h1>
    
    <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 40px; max-width: 1000px;">
        <div>
            <h2>Send a Message</h2>
            <form method="POST" action="/contact">
                <div style="margin-bottom: 20px;">
                    <label>Name:</label><br>
                    <input type="text" name="name" required style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
                </div>
                <div style="margin-bottom: 20px;">
                    <label>Email:</label><br>
                    <input type="email" name="email" required style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
                </div>
                <div style="margin-bottom: 20px;">
                    <label>Subject:</label><br>
                    <input type="text" name="subject" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px;">
                </div>
                <div style="margin-bottom: 20px;">
                    <label>Message:</label><br>
                    <textarea name="message" required rows="6" style="width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px;"></textarea>
                </div>
                <button type="submit" style="background: #667eea; color: white; padding: 12px 30px; border: none; border-radius: 4px; cursor: pointer;">Send Message</button>
            </form>
        </div>
        
        <div>
            <h2>Connect With Me</h2>
            <div style="margin-bottom: 20px;">
                <h3>📧 Email</h3>
                <p><a href="mailto:contact@vitthalmirji.com">contact@vitthalmirji.com</a></p>
            </div>
            <div style="margin-bottom: 20px;">
                <h3>💼 Professional</h3>
                <p><a href="https://linkedin.com/in/vitthalmirji">LinkedIn Profile</a></p>
                <p><a href="https://github.com/vim89">GitHub Profile</a></p>
            </div>
            <div style="margin-bottom: 20px;">
                <h3>📍 Location</h3>
                <p>Mumbai, India</p>
                <p>Available for remote work and consulting</p>
            </div>
            <div style="margin-bottom: 20px;">
                <h3>🚀 Valmuri Framework</h3>
                <p><a href="https://github.com/vim89/valmuri">Framework Repository</a></p>
                <p><a href="/blog">Technical Blog</a></p>
            </div>
        </div>
    </div>
</body>
</html>"""
  }

  private def handleContactSubmit(request: VRequest): VResult[String] = {
    // In a real implementation, this would send email or save to database
    VResult.success("""
<!DOCTYPE html>
<html>
<head><title>Thank You - Vitthal Mirji</title></head>
<body>
    <div style="text-align: center; padding: 50px;">
        <h1>Thank You!</h1>
        <p>Your message has been received. I'll get back to you within 24 hours.</p>
        <p><a href="/">Return to Home</a></p>
    </div>
</body>
</html>""")
  }

  private def renderBlogIndex(): String = {
    """<!DOCTYPE html>
<html>
<head><title>Blog - Vitthal Mirji</title></head>
<body>
    <h1>Technical Blog</h1>
    <p>Insights on software architecture, data engineering, and Scala development.</p>
    
    <article style="border-bottom: 1px solid #eee; padding: 20px 0;">
        <h2><a href="/blog/valmuri-framework">Building Valmuri: A Rails for Scala</a></h2>
        <p>Deep dive into the design decisions and architecture behind the Valmuri framework, and why Scala needed a productivity-focused web framework.</p>
        <time>January 15, 2025</time>
    </article>
    
    <article style="border-bottom: 1px solid #eee; padding: 20px 0;">
        <h2><a href="/blog/scala-web-development">The State of Scala Web Development in 2025</a></h2>
        <p>Comparison of existing Scala web frameworks and the opportunities for improvement in developer experience.</p>
        <time>January 10, 2025</time>
    </article>
</body>
</html>"""
  }

  private def renderValmuriBlogPost(): String = {
    """<!DOCTYPE html>
<html>
<head><title>Building Valmuri: A Rails for Scala</title></head>
<body>
    <nav><a href="/blog">← Back to Blog</a></nav>
    
    <article>
        <h1>Building Valmuri: A Rails for Scala</h1>
        <time>January 15, 2025</time>
        
        <h2>The Problem</h2>
        <p>After working with Django, Rails, and Spring Boot, I was frustrated by the state of Scala web development. While Scala has powerful libraries like http4s, ZIO, and Doobie, building web applications required assembling these libraries manually and writing significant boilerplate.</p>
        
        <h2>The Vision</h2>
        <p>What if Scala had a framework that provided:</p>
        <ul>
            <li><strong>Rails-level productivity</strong> with convention over configuration</li>
            <li><strong>Type safety</strong> throughout the entire request lifecycle</li>
            <li><strong>Functional programming</strong> benefits without complexity</li>
            <li><strong>30-minute deployment</strong> from idea to production</li>
        </ul>
        
        <h2>Key Design Decisions</h2>
        
        <h3>Auto-Configuration</h3>
        <p>Inspired by Spring Boot, Valmuri auto-configures everything based on sensible defaults. Just extend <code>VApplication</code> and define your routes.</p>
        
        <h3>Monadic Error Handling</h3>
        <p>Instead of exceptions, Valmuri uses <code>VResult[A]</code> for safe error handling that composes beautifully with functional programming patterns.</p>
        
        <h3>Type-Safe Routing</h3>
        <p>Route parameters are extracted and validated at compile time, eliminating runtime errors from invalid URLs.</p>
        
        <h2>Performance Results</h2>
        <ul>
            <li>50ms startup time vs 3000ms for Spring Boot</li>
            <li>25MB memory usage vs 250MB for typical Spring applications</li>
            <li>1000+ requests/second throughput</li>
        </ul>
        
        <h2>What's Next</h2>
        <p>The 0.1.0 MVP focuses on core functionality. Future releases will add advanced features like WebSockets, sophisticated ORM capabilities, and cloud-native deployment tools.</p>
        
        <p><a href="https://github.com/vim89/valmuri">Try Valmuri today</a> and let me know what you think!</p>
    </article>
</body>
</html>"""
  }

  private def renderScalaBlogPost(): String = {
    """<!DOCTYPE html>
<html>
<head><title>The State of Scala Web Development in 2025</title></head>
<body>
    <nav><a href="/blog">← Back to Blog</a></nav>
    
    <article>
        <h1>The State of Scala Web Development in 2025</h1>
        <time>January 10, 2025</time>
        
        <p>Scala web development has evolved significantly, but gaps remain in developer experience compared to other ecosystems.</p>
        
        <h2>Current Options</h2>
        
        <h3>Play Framework</h3>
        <p><strong>Pros:</strong> Mature, full-featured, good documentation</p>
        <p><strong>Cons:</strong> Complex setup, steep learning curve, heavyweight</p>
        
        <h3>http4s</h3>
        <p><strong>Pros:</strong> Pure functional, composable, lightweight</p>
        <p><strong>Cons:</strong> Requires assembly of multiple libraries, limited out-of-box features</p>
        
        <h3>ZIO HTTP</h3>
        <p><strong>Pros:</strong> Great ZIO integration, performant</p>
        <p><strong>Cons:</strong> ZIO-specific, newer ecosystem</p>
        
        <h2>The Gap</h2>
        <p>While these frameworks serve their purposes, none provide the "Rails experience" for Scala - where you can go from idea to deployed application in minimal time with maximum productivity.</p>
        
        <h2>Enter Valmuri</h2>
        <p>Valmuri aims to fill this gap by providing Rails-level productivity while maintaining Scala's strengths in type safety and functional programming.</p>
        
        <p>The future of Scala web development is bright, and I believe frameworks like Valmuri will make Scala more accessible to web developers coming from other ecosystems.</p>
    </article>
</body>
</html>"""
  }

  def main(args: Array[String]): Unit = {
    start() match {
      case VResult.Success(_) =>
        println("✅ Personal site running at http://localhost:8080")
        println("📝 Blog available at http://localhost:8080/blog")
        println("📞 Contact form at http://localhost:8080/contact")
        Thread.currentThread().join()
      case VResult.Failure(error) =>
        println(s"❌ Failed to start: ${error.message}")
    }
  }
}
