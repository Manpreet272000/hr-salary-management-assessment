package com.acme.hr.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Keeps the MVC layering honest: controller → service → dao, with model shared by all. A violation
 * (e.g. a controller calling a repository) fails the build instead of slipping into review.
 */
@AnalyzeClasses(packages = "com.acme.hr", importOptions = ImportOption.DoNotIncludeTests.class)
class LayeredArchitectureTest {

    @ArchTest
    static final ArchRule layersAreRespected = layeredArchitecture()
            .consideringOnlyDependenciesInLayers()
            .layer("Controller").definedBy("..controller..")
            .layer("Service").definedBy("..service..")
            .layer("Aspect").definedBy("..aspect..")
            .layer("Dao").definedBy("..dao..")
            .layer("Seed").definedBy("..seed..")
            .layer("Model").definedBy("..model..")
            .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
            .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller")
            .whereLayer("Dao").mayOnlyBeAccessedByLayers("Service", "Aspect", "Seed")
            .whereLayer("Seed").mayNotBeAccessedByAnyLayer();

    @ArchTest
    static final ArchRule modelDependsOnNothingAboveIt = noClasses()
            .that().resideInAPackage("..model..")
            .should().dependOnClassesThat().resideInAnyPackage("..controller..", "..service..", "..dao..", "..aspect..");

    @ArchTest
    static final ArchRule controllersOnlyTalkToServices = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAnyPackage("..dao..", "..model.entity..");

    @ArchTest
    static final ArchRule noPackageCycles = slices().matching("com.acme.hr.(*)..").should().beFreeOfCycles();
}
