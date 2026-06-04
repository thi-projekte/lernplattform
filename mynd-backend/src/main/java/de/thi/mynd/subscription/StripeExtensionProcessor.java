package de.thi.mynd.subscription;

import io.quarkus.deployment.annotations.BuildProducer;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.builditem.CombinedIndexBuildItem;
import io.quarkus.deployment.builditem.nativeimage.ReflectiveClassBuildItem;
import org.jboss.jandex.ClassInfo;

public class StripeExtensionProcessor {

  @BuildStep
  void registerPackageForReflection(
      CombinedIndexBuildItem combinedIndexBuildItem,
      BuildProducer<ReflectiveClassBuildItem> reflectiveClass) {

    String targetPackage = "com.stripe";

    // Iterate through all indexed classes and match the package prefix
    for (ClassInfo classInfo : combinedIndexBuildItem.getIndex().getKnownClasses()) {
      if (classInfo.name().toString().startsWith(targetPackage)) {
        reflectiveClass.produce(
            ReflectiveClassBuildItem.builder(classInfo.name().toString())
                .methods(true)
                .constructors(true)
                .publicConstructors(true)
                .fields(true)
                .build());
      }
    }
  }
}
