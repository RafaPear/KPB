package pt.rafap.kpb.core.storage.serializers

import org.junit.Test
import pt.rafap.kpb.core.storage.CoreState
import kotlin.test.assertFails

class GameStateSerializerTest {
    val testUnit = SerializerTestUnit(CoreStateSerializer()) {
        val list = mutableListOf<CoreState>()
        val ids = 1..100
        for (id in ids) list.add(CoreState(id))
        list
    }

    val testingCoreState = CoreState(1)
    @Test
    fun `Test serialize and deserialize`() {
        testUnit.runTest()
    }

    @Test
    fun `Test serialize`() {
        val serialized = CoreStateSerializer().serialize(testingCoreState)
        val expected = testingCoreState.id.toString()
        assert(serialized == expected)
    }

    @Test
    fun `Test deserialize`() {
        val data = testingCoreState.id.toString()
        val deserialized = CoreStateSerializer().deserialize(data)
        val expected = testingCoreState
        assert(deserialized == expected)
    }

    @Test
    fun `Deserialize bad data throws exception`() {
        val badData1 = ""
        val badData2 = "abc"
        val badData3 = "12.34"
        val badData4 = "-56"
        val badData5 = "1a2b3c"
        val badData6 = " "
        val badData7 = "9999999999999999999999999"
        val badData8 = "\n"
        val badDataList = listOf(badData1, badData2, badData3, badData4, badData5, badData6, badData7, badData8)

        for (badData in badDataList) {
            assertFails {
                CoreStateSerializer().deserialize(badData)
            }
        }
    }
}