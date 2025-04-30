package edu.chorn.myproject;

/*
    @author chorn
    @project myproject
    @class ProjectArchitectureTests
    @version 1.0.0
    @since 19.04.2025 - 16.11
*/

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Service;
import org.springframework.stereotype.Repository;
import edu.chorn.myproject.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

@SpringBootTest
public class ProjectArchitectureTests {

    private JavaClasses classes;

    @BeforeEach
    void setUp() {

        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_ARCHIVES)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_JARS)
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("edu.chorn.myproject");
    }

    @Test
    void shouldDefineControllerLayer() {
        layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controller").definedBy("..controller..")
                .check(classes);
    }

    @Test
    void shouldDefineServiceLayer() {
        layeredArchitecture()
                .consideringAllDependencies()
                .layer("Service").definedBy("..service..")
                .check(classes);
    }

    @Test
    void shouldDefineRepositoryLayer() {
        layeredArchitecture()
                .consideringAllDependencies()
                .layer("Repository").definedBy("..repository..")
                .check(classes);
    }

    @Test
    void controllerLayerShouldNotBeAccessedByAnyLayer() {
        layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controller").definedBy("..controller..")
                .whereLayer("Controller").mayNotBeAccessedByAnyLayer()
                .check(classes);
    }

    @Test
    void serviceLayerShouldOnlyBeAccessedByControllerAndServiceLayers() {
        layeredArchitecture()
                .consideringAllDependencies()
                .layer("Controller").definedBy("..controller..")
                .layer("Service").definedBy("..service..")
                .whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service")
                .check(classes);
    }

    @Test
    void repositoryLayerShouldOnlyBeAccessedByServiceLayer() {
        layeredArchitecture()
                .consideringAllDependencies()
                .layer("Repository").definedBy("..repository..")
                .layer("Service").definedBy("..service..")
                //
                .whereLayer("Repository").mayOnlyBeAccessedByLayers("Service")
                .check(classes);
    }

    @Test
    void controllersShouldBeNamedXController() {
        classes()
                .that().resideInAPackage("..controller..")
                .should().haveSimpleNameEndingWith("Controller")
                .check(classes);
    }

    @Test
    void controllersShouldBeAnnotatedWithRestController() {
        classes()
                .that().resideInAPackage("..controller..")
                .should().beAnnotatedWith(RestController.class)
                .check(classes);
    }

    @Test
    void controllersShouldBeAnnotatedWithRequestMapping() {
        classes()
                .that().resideInAPackage("..controller..")
                .should().beAnnotatedWith(RequestMapping.class)
                .check(classes);
    }

    @Test
    void controllersShouldNotHaveAutowiredFields() {
        noFields()
                .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
                .should().beAnnotatedWith(Autowired.class)
                .check(classes);
    }

    @Test
    void controllersShouldOnlyDependOnServiceLayer() {
        noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .check(classes);
    }

    @Test
    void controllerMethodsShouldBePublic() {
        methods()
                .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
                .should().bePublic()
                .check(classes);
    }

    @Test
    void controllersShouldNotDependOnOtherControllers() {
        noClasses()
                .that().resideInAPackage("..controller..")
                .should().dependOnClassesThat().resideInAPackage("..controller..")
                .check(classes);
    }

    @Test
    void controllersShouldNotBeInterfaces() {
        noClasses()
                .that().resideInAPackage("..controller..")
                .should().beInterfaces()
                .check(classes);
    }

    @Test
    void servicesShouldBeNamedXService() {
        classes()
                .that().resideInAPackage("..service..")
                .should().haveSimpleNameEndingWith("Service")
                .check(classes);
    }

    @Test
    void servicesShouldBeAnnotatedWithService() {
        classes()
                .that().resideInAPackage("..service..")
                .should().beAnnotatedWith((Class<? extends Annotation>) Service.class)
                .check(classes);
    }

    @Test
    void servicesShouldNotHaveAutowiredFields() {
        noFields()
                .that().areDeclaredInClassesThat().resideInAPackage("..service..")
                .should().beAnnotatedWith(Autowired.class)
                .check(classes);
    }

    @Test
    void servicesShouldNotDependOnControllers() {
        noClasses()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAPackage("..controller..")
                .check(classes);
    }

    @Test
    void servicesShouldDependOnRepositories() {
        classes()
                .that().resideInAPackage("..service..")
                .should().dependOnClassesThat().resideInAPackage("..repository..")
                .check(classes);
    }

    @Test
    void servicesShouldNotBeInterfaces() {
        noClasses()
                .that().resideInAPackage("..service..")
                .should().beInterfaces()
                .check(classes);
    }

    @Test
    void repositoriesShouldBeInterfaces() {
        classes()
                .that().resideInAPackage("..repository..")
                .should().beInterfaces()
                .check(classes);
    }

    @Test
    void repositoriesShouldBeAnnotatedWithRepository() {
        classes()
                .that().resideInAPackage("..repository..")
                .should().beAnnotatedWith((Class<? extends Annotation>) Repository.class)
                .check(classes);
    }

    @Test
    void repositoriesShouldBeNamedXRepository() {
        classes()
                .that().resideInAPackage("..repository..")
                .should().haveSimpleNameEndingWith("Repository")
                .check(classes);
    }

    @Test
    void repositoriesShouldExtendMongoRepository() {
        classes()
                .that().resideInAPackage("..repository..")
                .should().beAssignableTo(MongoRepository.class)
                .check(classes);
    }

    @Test
    void repositoriesShouldNotDependOnServices() {
        noClasses()
                .that().resideInAPackage("..repository..")
                .should().dependOnClassesThat().resideInAPackage("..service..")
                .check(classes);
    }

    @Test
    void repositoriesShouldNotDependOnControllers() {
        noClasses()
                .that().resideInAPackage("..repository..")
                .should().dependOnClassesThat().resideInAPackage("..controller..")
                .check(classes);
    }

    @Test
    void repositoriesShouldBePublic() {
        noClasses()
                .that().resideInAPackage("..repository..")
                .should().bePrivate()
                .check(classes);
    }

    @Test
    void modelsShouldBeAnnotatedWithDocument() {
        classes()
                .that().resideInAPackage("..model..")
                .and().haveSimpleNameNotContaining("Builder")
                .should().beAnnotatedWith(Document.class)
                .check(classes);
    }

    @Test
    void modelIdFieldShouldHaveIdAnnotation() {
        fields()
                .that().areDeclaredInClassesThat()
                .areAnnotatedWith(Document.class)
                .and().haveName("id")
                .should().beAnnotatedWith(Id.class)
                .check(classes);
    }

    @Test
    void modelFieldsShouldBePrivate() {
        fields()
                .that().areDeclaredInClassesThat().resideInAPackage("..model..")
                .should().notBePublic()
                .check(classes);
    }

    @Test
    void modelClassesShouldNotDependOnControllers() {
        noClasses()
                .that().resideInAPackage("..model..")
                .should().dependOnClassesThat()
                .resideInAPackage("..controller..")
                .check(classes);
    }

    @Test
    void modelClassesShouldNotDependOnServices() {
        noClasses()
                .that().resideInAPackage("..model..")
                .should().dependOnClassesThat()
                .resideInAPackage("..service..")
                .check(classes);
    }

    @Test
    void modelClassesShouldNotDependOnRepositories() {
        noClasses()
                .that().resideInAPackage("..model..")
                .should().dependOnClassesThat()
                .resideInAPackage("..repository..")
                .check(classes);
    }

    @Test
    void modelClassesShouldBePublic() {
        classes()
                .that().resideInAPackage("..model..")
                .should().bePublic()
                .check(classes);
    }

    @Test
    void userModelOverridesEquals() throws NoSuchMethodException {
        Class<?> clazz = User.class;
        clazz.getDeclaredMethod("equals", Object.class);
    }

    @Test
    void userModelOverridesHashCode() throws NoSuchMethodException {
        Class<?> clazz = User.class;
        clazz.getDeclaredMethod("hashCode");
    }

    @Test
    void applicationClassShouldBeAnnotatedWithSpringBootApplication() {
        classes()
                .that().haveSimpleNameEndingWith("Application")
                .should().beAnnotatedWith(SpringBootApplication.class)
                .check(classes);
    }

    @Test
    void onlyOneSpringBootApplicationClass() {
        classes()
                .that().areAnnotatedWith(SpringBootApplication.class)
                .should().haveSimpleName("MyprojectApplication")
                .check(classes);
    }
}