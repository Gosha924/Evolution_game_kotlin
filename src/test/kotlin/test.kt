import org.junit.platform.suite.api.SelectClasses
import org.junit.platform.suite.api.Suite

@Suite
@SelectClasses(
    EvolutionIntegrationTest::class,
    GameRepositoryTest::class,
    GameTurnOrderTest::class,
    MimicryTest::class,
    PairInteractionTest::class,
    PiracyTest::class,
    PoisonousTraitTest::class,
    RunningTraitTest::class,
    StatisticsTest::class,
    TailLossTest::class
)
class AllTestsSuite