package de.thi.mynd.subscription;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.jboss.logging.Logger;

public class StripeExtensionProcessor {

  private static final Logger LOG = Logger.getLogger(StripeExtensionProcessor.class);
  private static final String FEATURE = "stripe-java";

  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(FEATURE);
  }

  @BuildStep
  void registerStripeClassesForReflection(BuildProducer<ReflectiveClassBuildItem> reflective) {
    List<String> classNames = new ArrayList<>();

    try {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      List<URL> urls = collectUrls(cl);

      for (URL url : urls) {
        try {
          URI uri = url.toURI();
          File file = new File(uri);

          if (!file.getName().contains("stripe-java") || !file.getName().endsWith(".jar")) {
            continue;
          }

          LOG.infof("Scanning stripe JAR for reflection config: %s", file.getName());

          try (JarFile jar = new JarFile(file)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
              JarEntry entry = entries.nextElement();
              String name = entry.getName();

              if (!name.endsWith(".class")
                  || name.contains("$anon")
                  || name.contains("package-info")) {
                continue;
              }

              String className =
                  name.replace('/', '.').substring(0, name.length() - ".class".length());

              if (isStripeClass(className)) {
                classNames.add(className);
              }
            }
          }

        } catch (Exception e) {
          LOG.warnf("Could not scan URL %s: %s", url, e.getMessage());
        }
      }

    } catch (Exception e) {
      LOG.errorf("Failed to scan stripe-java JAR for reflection registration: %s", e.getMessage());
    }

    if (classNames.isEmpty()) {
      LOG.warn(
          "No Stripe classes found for reflection registration — native image may fail at runtime");
      return;
    }

    LOG.infof("Registering %d Stripe classes for reflection", classNames.size());

    reflective.produce(
        ReflectiveClassBuildItem.builder(classNames.toArray(String[]::new))
            .constructors(true)
            .methods(true)
            .fields(true)
            .build());
  }

  private boolean isStripeClass(String className) {
    return className.startsWith("com.stripe.model")
        || className.startsWith("com.stripe.param")
        || className.startsWith("com.stripe.net")
        || className.startsWith("com.stripe.exception")
        || className.startsWith("com.stripe.service");
  }

  /**
   * Walks the classloader hierarchy to collect all URLs. Handles both URLClassLoader and Quarkus's
   * own classloader wrappers.
   */
  private List<URL> collectUrls(ClassLoader cl) throws Exception {
    List<URL> urls = new ArrayList<>();
    ClassLoader current = cl;

    while (current != null) {
      if (current instanceof URLClassLoader urlCl) {
        for (URL url : urlCl.getURLs()) {
          urls.add(url);
        }
      } else {
        // Quarkus 3.x uses a non-URLClassLoader — fall back to getResources
        // to locate the stripe JAR via a known class it contains
        try {
          Enumeration<URL> resources = current.getResources("com/stripe/Stripe.class");
          while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String resourceUrl = resource.toString();
            // Extract the JAR path from jar:file:/path/to/stripe.jar!/com/stripe/Stripe.class
            if (resourceUrl.startsWith("jar:")) {
              String jarPath = resourceUrl.substring("jar:".length(), resourceUrl.indexOf("!/"));
              urls.add(new URL(jarPath));
            }
          }
        } catch (Exception ignored) {
        }
      }
      current = current.getParent();
    }

    return urls;
  }
}
