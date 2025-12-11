package tqs.sportslink.functionals;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.*;

@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("tqs/sportslink/functionals") // Os arquivos .feature devem estar em
                                                       // src/test/resources/tqs/sportslink
@ConfigurationParameter(key = PLUGIN_PROPERTY_NAME, value = "pretty, json:target/cucumber-reports/cucumber.json")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "tqs.sportslink.functionals") // Os testes devem estar em
                                                                                        // tqs.sportlink.functionals
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @skip")
class RunCucumberTest {
}
