import com.pingidentity.davinci.collector.FieldCollector
import com.pingidentity.davinci.collector.FlowCollector
import com.pingidentity.testrail.TestRailCase
import com.pingidentity.testrail.TestRailWatcher
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Test
import org.junit.Assert.*
import org.junit.Rule
import org.junit.rules.TestWatcher

class FlowCollectorTest {

    @JvmField
    @Rule
    val watcher: TestWatcher = TestRailWatcher

    @TestRailCase(21258)
    @Test
    fun testInitialization() {
        val flowCollector = FlowCollector()
        assertNotNull(flowCollector)
    }

    @TestRailCase(22145)
    @Test
    fun testInheritance() {
        val flowCollector = FlowCollector()
        assertTrue(flowCollector is FieldCollector)
    }

    @TestRailCase(22146)
    @Test
    fun `should initialize key and label from JsonObject`() {
        val flowCollector = FlowCollector()
        val jsonObject = buildJsonObject {
            put("key", "testKey")
            put("label", "testLabel")
        }

        flowCollector.init(jsonObject)

        kotlin.test.assertEquals("testKey", flowCollector.key)
        kotlin.test.assertEquals("testLabel", flowCollector.label)
    }

    @TestRailCase(22147)
    @Test
    fun `should return value when value is set`() {
        val flowCollector = FlowCollector()
        flowCollector.value = "test"
        kotlin.test.assertEquals("test", flowCollector.value)
    }
}