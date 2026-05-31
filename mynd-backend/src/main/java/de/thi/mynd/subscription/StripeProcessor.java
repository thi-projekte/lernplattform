package de.thi.mynd.subscription;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.IndexDependencyBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.IndexView;

public class StripeProcessor {

  private static final String FEATURE = "stripe";

  @BuildStep
  FeatureBuildItem feature() {
    return new FeatureBuildItem(FEATURE);
  }

  @BuildStep
  void addDependencies(BuildProducer<IndexDependencyBuildItem> indexDependency) {
    indexDependency.produce(new IndexDependencyBuildItem("com.stripe", "stripe-java"));
  }

  @BuildStep
  public void build(
      BuildProducer<ReflectiveClassBuildItem> reflectiveClass,
      CombinedIndexBuildItem indexBuildItem) {
    IndexView index = indexBuildItem.getIndex();
    for (ClassInfo clazz : index.getKnownClasses()) {
      if (clazz.name().toString().startsWith("com.stripe.model")) {
        reflectiveClass.produce(
            ReflectiveClassBuildItem.builder(clazz.name().toString())
                .fields(true)
                .serialization(true)
                .unsafeAllocated(true)
                .build());
      }
    }
  }
}
