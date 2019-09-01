<map version="1.0.1">
<!-- To view this file, download free mind mapping software FreeMind from http://freemind.sourceforge.net -->
<node COLOR="#000000" CREATED="1566756216575" ID="ID_629424246" MODIFIED="1566756236734" TEXT="Quilly Jumper">
<font NAME="SansSerif" SIZE="20"/>
<hook NAME="accessories/plugins/AutomaticLayout.properties"/>
<node COLOR="#0033ff" CREATED="1566756247602" ID="ID_1285952926" MODIFIED="1566756911183" POSITION="right" TEXT="ToDo">
<edge STYLE="sharp_bezier" WIDTH="8"/>
<font NAME="SansSerif" SIZE="18"/>
<node COLOR="#00b439" CREATED="1566756275724" FOLDED="true" ID="ID_1120160653" MODIFIED="1566837819346" TEXT="create KeyBoardController">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#990000" CREATED="1566756293584" ID="ID_1591613942" MODIFIED="1566756298585" TEXT="Inputmultiplexer needed">
<font NAME="SansSerif" SIZE="14"/>
<node COLOR="#111111" CREATED="1566756299068" ID="ID_895853694" MODIFIED="1566756300161" TEXT="Stage"/>
<node COLOR="#111111" CREATED="1566756301093" ID="ID_1487040556" MODIFIED="1566756304281" TEXT="KeyboardController"/>
</node>
<node COLOR="#990000" CREATED="1566757233265" ID="ID_587790964" MODIFIED="1566757238375" TEXT="remove test stuff from GameScreen">
<font NAME="SansSerif" SIZE="14"/>
</node>
</node>
<node COLOR="#00b439" CREATED="1566756331927" FOLDED="true" ID="ID_1239642524" MODIFIED="1566843968223" TEXT="add entity jump logic">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#990000" CREATED="1566757280300" ID="ID_838820531" MODIFIED="1566757287334" TEXT="JumpComponent">
<font NAME="SansSerif" SIZE="14"/>
<node COLOR="#111111" CREATED="1566757288310" ID="ID_878434856" MODIFIED="1566757291857" TEXT="JumpDirection">
<node COLOR="#111111" CREATED="1566757292360" ID="ID_244614978" MODIFIED="1566757295657" TEXT="JUMP"/>
<node COLOR="#111111" CREATED="1566757296983" ID="ID_955989534" MODIFIED="1566757298139" TEXT="FALL"/>
<node COLOR="#111111" CREATED="1566757299193" ID="ID_1967316789" MODIFIED="1566757300333" TEXT="STOP"/>
</node>
</node>
<node COLOR="#990000" CREATED="1566756375077" ID="ID_31186968" MODIFIED="1566757317978" TEXT="JumpSystem similar to moveSystem">
<font NAME="SansSerif" SIZE="14"/>
</node>
</node>
<node COLOR="#00b439" CREATED="1566756767831" FOLDED="true" ID="ID_880607672" MODIFIED="1566846854252" TEXT="add PlayerComponent">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#990000" CREATED="1566756773026" ID="ID_1813433416" MODIFIED="1566756776104" TEXT="add it to player entity">
<font NAME="SansSerif" SIZE="14"/>
</node>
</node>
<node COLOR="#00b439" CREATED="1566919363979" FOLDED="true" ID="ID_718044140" MODIFIED="1566922298344" TEXT="revert the val Entity. stuff because it seems like the compiler does not optimize it and therefore we have then multiple entity.get(cmp) calls --&gt; bad performance">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#990000" CREATED="1566919409836" ID="ID_1977007630" MODIFIED="1566919416150" TEXT="use &quot;let&quot; calls again">
<font NAME="SansSerif" SIZE="14"/>
</node>
</node>
<node COLOR="#00b439" CREATED="1566756719166" FOLDED="true" ID="ID_1588828756" MODIFIED="1566927332888" TEXT="add EntityTypeComponent">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#990000" CREATED="1566756729593" ID="ID_1661171165" MODIFIED="1566756748157" TEXT="create floor and water as an entity">
<font NAME="SansSerif" SIZE="14"/>
</node>
<node COLOR="#990000" CREATED="1566757144563" ID="ID_1083915927" MODIFIED="1566757145579" TEXT="types">
<font NAME="SansSerif" SIZE="14"/>
<node COLOR="#111111" CREATED="1566757154980" ID="ID_1635665824" MODIFIED="1566757156823" TEXT="player"/>
<node COLOR="#111111" CREATED="1566757157992" ID="ID_1098382857" MODIFIED="1566757158899" TEXT="enemy"/>
<node COLOR="#111111" CREATED="1566757161360" ID="ID_1084119785" MODIFIED="1566757163988" TEXT="scenery"/>
<node COLOR="#111111" CREATED="1566757172428" ID="ID_1199758273" MODIFIED="1566757174051" TEXT="item"/>
<node COLOR="#111111" CREATED="1566757165930" ID="ID_1411618001" MODIFIED="1566757169555" TEXT="other"/>
</node>
</node>
<node COLOR="#00b439" CREATED="1566757357801" FOLDED="true" ID="ID_1678291687" MODIFIED="1566930787670" TEXT="add collision logic">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#990000" CREATED="1566756777509" ID="ID_894238715" MODIFIED="1566928156175" TEXT="add CollisionComponent">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="14"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#111111" CREATED="1566756782955" ID="ID_835230966" MODIFIED="1566757365664" TEXT="stores list of entities">
<font NAME="SansSerif" SIZE="12"/>
</node>
<node COLOR="#111111" CREATED="1566756851133" ID="ID_888044930" MODIFIED="1566757365664" TEXT="adjust beginContact/endContact in ContactListener to add/remove entities from this list for the player entity">
<font NAME="SansSerif" SIZE="12"/>
</node>
</node>
<node COLOR="#990000" CREATED="1566756796152" ID="ID_1705175012" MODIFIED="1566928158442" TEXT="add PlayerCollisionSystem">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="14"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#111111" CREATED="1566756809478" ID="ID_1225973656" MODIFIED="1566757365680" TEXT="log messages when colliding with a specific entity type">
<font NAME="SansSerif" SIZE="12"/>
</node>
</node>
<node COLOR="#990000" CREATED="1566846905194" ID="ID_1417362042" MODIFIED="1566930782541" TEXT="add ground sensor for jump system">
<font NAME="SansSerif" SIZE="14"/>
<icon BUILTIN="button_ok"/>
</node>
</node>
<node COLOR="#00b439" CREATED="1566759034211" FOLDED="true" ID="ID_381483062" MODIFIED="1567009646944" TEXT="add sound for jump">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#990000" CREATED="1566759043501" ID="ID_1322492472" MODIFIED="1566759047329" TEXT="play sound when jump">
<font NAME="SansSerif" SIZE="14"/>
</node>
</node>
<node COLOR="#00b439" CREATED="1566757369356" FOLDED="true" ID="ID_140819722" MODIFIED="1567242740350" TEXT="add animation logic">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#990000" CREATED="1566757591240" ID="ID_1571591329" MODIFIED="1567207011276" TEXT="add AnimationCfgs">
<font NAME="SansSerif" SIZE="14"/>
<icon BUILTIN="stop-sign"/>
<node COLOR="#111111" CREATED="1566757596859" ID="ID_922723291" MODIFIED="1566757718100" TEXT="create simple DSL to describe an animation"/>
<node COLOR="#111111" CREATED="1566758329232" ID="ID_465879660" MODIFIED="1566758341734" TEXT="custom Animation class needed that contains model and animationType information"/>
</node>
<node COLOR="#990000" CREATED="1566756912373" FOLDED="true" ID="ID_1859438504" MODIFIED="1567207018177" TEXT="add AnimationComponent">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="14"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#111111" CREATED="1566756918990" ID="ID_1330604124" MODIFIED="1566757376573" TEXT="Animation&lt;TextureRegion&gt;">
<font NAME="SansSerif" SIZE="12"/>
</node>
<node COLOR="#111111" CREATED="1566756940977" ID="ID_1525579307" MODIFIED="1566757376573" TEXT="animationTime">
<font NAME="SansSerif" SIZE="12"/>
</node>
<node COLOR="#111111" CREATED="1566757479207" ID="ID_184920335" MODIFIED="1566757484692" TEXT="model : ModelType">
<node COLOR="#111111" CREATED="1566757485585" ID="ID_975756398" MODIFIED="1566757488276" TEXT="ModelTypes">
<node COLOR="#111111" CREATED="1566757488701" ID="ID_945401275" MODIFIED="1566757491174" TEXT="PLAYER"/>
</node>
</node>
<node COLOR="#111111" CREATED="1566757501659" ID="ID_1434149497" MODIFIED="1566757513644" TEXT="animationType:AnimationType">
<node COLOR="#111111" CREATED="1566757514622" ID="ID_365282279" MODIFIED="1566757947413" TEXT="AnimationTypes">
<node COLOR="#111111" CREATED="1566757518204" ID="ID_370695849" MODIFIED="1566757519376" TEXT="IDLE"/>
<node COLOR="#111111" CREATED="1566757520391" ID="ID_1968036005" MODIFIED="1566757521642" TEXT="WALK"/>
<node COLOR="#111111" CREATED="1566757948981" ID="ID_822067674" MODIFIED="1566757950044" TEXT="JUMP"/>
<node COLOR="#111111" CREATED="1566757951287" ID="ID_1956030429" MODIFIED="1566757952303" TEXT="FALL"/>
</node>
</node>
</node>
<node COLOR="#990000" CREATED="1566756944754" FOLDED="true" ID="ID_1241458087" MODIFIED="1567207031930" TEXT="add AnimationSystem">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="14"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#111111" CREATED="1566756950536" ID="ID_1354249477" MODIFIED="1566757376589" TEXT="updates animation">
<font NAME="SansSerif" SIZE="12"/>
<node COLOR="#111111" CREATED="1566758365965" ID="ID_1990580397" MODIFIED="1566758386545" TEXT="checks if animation.model and animation.type are equal to the component. If not it retrieves the new animation"/>
</node>
<node COLOR="#111111" CREATED="1566757003929" ID="ID_1046481348" MODIFIED="1566757376589" TEXT="updates the sprite of the RenderComponent">
<font NAME="SansSerif" SIZE="12"/>
<node COLOR="#111111" CREATED="1566757575094" ID="ID_1104290609" MODIFIED="1566757588861" TEXT="sets error image in case model/animationType are not part of AnimationCache"/>
</node>
<node COLOR="#111111" CREATED="1566757632665" ID="ID_1208610979" MODIFIED="1567207027975" TEXT="parameter is AnimationCfgs">
<icon BUILTIN="stop-sign"/>
</node>
<node COLOR="#111111" CREATED="1566757733751" ID="ID_1644381456" MODIFIED="1566757739299" TEXT="contains an animationCache">
<node COLOR="#111111" CREATED="1566757739967" ID="ID_489182471" MODIFIED="1566757752312" TEXT="if cache does not contain model/animation then it gets loaded via animationCfg"/>
</node>
</node>
<node COLOR="#990000" CREATED="1566757387593" FOLDED="true" ID="ID_1977994125" MODIFIED="1567207037790" TEXT="pack idle and walk animation of player">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="14"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#111111" CREATED="1566757400186" ID="ID_78883975" MODIFIED="1566757836544" TEXT="TextureAtlasAsset">
<font NAME="SansSerif" SIZE="12"/>
<node COLOR="#111111" CREATED="1566757406548" ID="ID_942027535" MODIFIED="1566757410579" TEXT="add to LoadingScreen"/>
</node>
</node>
<node COLOR="#990000" CREATED="1567207039867" ID="ID_492759856" MODIFIED="1567242736368" TEXT="ToDo">
<font NAME="SansSerif" SIZE="14"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#111111" CREATED="1567207044305" FOLDED="true" ID="ID_1219020415" MODIFIED="1567241052862" TEXT="Fix Animation constructor to make textureRegions optional">
<icon BUILTIN="button_ok"/>
<node COLOR="#111111" CREATED="1567207081843" ID="ID_505607965" MODIFIED="1567207089306" TEXT="call  setKeyFrames in init method of class"/>
<node COLOR="#111111" CREATED="1567207434734" ID="ID_451882125" MODIFIED="1567207442990" TEXT="update constructor call of AnimationComponent afterwards"/>
</node>
<node COLOR="#111111" CREATED="1567207219641" FOLDED="true" ID="ID_953245415" MODIFIED="1567241076168" TEXT="loadAllAnimations">
<icon BUILTIN="stop-sign"/>
<node COLOR="#111111" CREATED="1567207223060" ID="ID_1563036534" MODIFIED="1567207233572" TEXT="optimize findRegions so that it does not create a new array instance all the time"/>
<node COLOR="#111111" CREATED="1567207234509" ID="ID_490630779" MODIFIED="1567207241763" TEXT="e.g. create extension method with an array parameter">
<node COLOR="#111111" CREATED="1567207395125" ID="ID_1408391261" MODIFIED="1567207414574" TEXT="pass in the regionKeys array from the parameters of the loader"/>
</node>
<node COLOR="#111111" CREATED="1567207720037" ID="ID_1209886802" MODIFIED="1567207733001" TEXT="extract fileName key creation to a method because it is used in two different places"/>
</node>
<node COLOR="#111111" CREATED="1567207312816" ID="ID_1773116224" MODIFIED="1567241085230" TEXT="add some debug and error log messages to Animation.kt">
<icon BUILTIN="stop-sign"/>
</node>
<node COLOR="#111111" CREATED="1567207477278" FOLDED="true" ID="ID_1036158918" MODIFIED="1567241586578" TEXT="AnimationSystem">
<icon BUILTIN="button_ok"/>
<node COLOR="#111111" CREATED="1567207480252" ID="ID_1895841074" MODIFIED="1567207492103" TEXT="find a cleaner solution to set the first animation instead of using keyframes.isEmpty()"/>
<node COLOR="#111111" CREATED="1567207535253" ID="ID_706140362" MODIFIED="1567207553147" TEXT="if textureRegion is null for getKeyFrames then print an error message and use the error texture as fallback"/>
</node>
<node COLOR="#111111" CREATED="1567207608088" FOLDED="true" ID="ID_1463354305" MODIFIED="1567241592521" TEXT="Known Bug:">
<icon BUILTIN="button_ok"/>
<node COLOR="#111111" CREATED="1567207611055" ID="ID_1047405096" MODIFIED="1567207619490" TEXT="red rectangles are drawn outside of box2d bodies - why?"/>
</node>
<node COLOR="#111111" CREATED="1567207939181" ID="ID_1431111896" MODIFIED="1567242714605" TEXT="go through existing files due to the new codestyle and add some comments">
<icon BUILTIN="button_ok"/>
</node>
<node COLOR="#111111" CREATED="1567208192499" ID="ID_535673349" MODIFIED="1567242733777" TEXT="create MIT license">
<icon BUILTIN="button_ok"/>
</node>
</node>
</node>
<node COLOR="#00b439" CREATED="1566757847427" FOLDED="true" ID="ID_913038838" MODIFIED="1567334409085" TEXT="add state logic">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#990000" CREATED="1566757850474" ID="ID_610321452" MODIFIED="1566757854053" TEXT="add StateComponent">
<font NAME="SansSerif" SIZE="14"/>
<node COLOR="#111111" CREATED="1566757922383" ID="ID_1788406565" MODIFIED="1566757923774" TEXT="state">
<node COLOR="#111111" CREATED="1566757924602" ID="ID_809682809" MODIFIED="1566757925712" TEXT="States">
<node COLOR="#111111" CREATED="1566757926601" ID="ID_1611989844" MODIFIED="1566757930392" TEXT="IDLE"/>
<node COLOR="#111111" CREATED="1566757931340" ID="ID_325951445" MODIFIED="1566757932505" TEXT="WALK"/>
<node COLOR="#111111" CREATED="1566757939760" ID="ID_411289816" MODIFIED="1566757940854" TEXT="JUMP"/>
<node COLOR="#111111" CREATED="1566757943491" ID="ID_1025667458" MODIFIED="1566757944553" TEXT="FALL"/>
</node>
</node>
</node>
<node COLOR="#990000" CREATED="1566757868491" ID="ID_1858494542" MODIFIED="1566757871397" TEXT="add StateSystem">
<font NAME="SansSerif" SIZE="14"/>
<node COLOR="#111111" CREATED="1566757873237" ID="ID_1016056919" MODIFIED="1566758897524" TEXT="checks">
<node COLOR="#111111" CREATED="1566758898741" ID="ID_1523674604" MODIFIED="1566758917771" TEXT="move.speed == 0 &amp;&amp; jump.speed == 0 --&gt; IDLE"/>
<node COLOR="#111111" CREATED="1566758919899" ID="ID_884025824" MODIFIED="1566758925087" TEXT="move.speed != 0 --&gt; WALK"/>
<node COLOR="#111111" CREATED="1566758927261" ID="ID_1026117406" MODIFIED="1566758944041" TEXT="jump.speed &gt; 0 --&gt; JUMP"/>
<node COLOR="#111111" CREATED="1566758945262" ID="ID_1601916990" MODIFIED="1566758950888" TEXT="jump.speed &lt; 0 --&gt; FALL"/>
</node>
<node COLOR="#111111" CREATED="1567242754101" ID="ID_1431759126" MODIFIED="1567242769149" TEXT="also sets the animation of the entity and flips the sprite if needed (e.g. move left/right)"/>
</node>
</node>
<node COLOR="#00b439" CREATED="1566846092724" FOLDED="true" ID="ID_1150117663" MODIFIED="1567364644650" TEXT="add DSL for gameObjects">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<icon BUILTIN="button_ok"/>
<node COLOR="#990000" CREATED="1566846100447" ID="ID_1838114735" MODIFIED="1566846128581" TEXT="to descriibe width/height/modelType/moveSpeed/jumpSpeed/entityType/...">
<font NAME="SansSerif" SIZE="14"/>
</node>
</node>
<node COLOR="#00b439" CREATED="1567364650728" ID="ID_201333319" MODIFIED="1567364654614" TEXT="Optimizations">
<edge STYLE="bezier" WIDTH="thin"/>
<font NAME="SansSerif" SIZE="16"/>
<node COLOR="#990000" CREATED="1567364654911" ID="ID_787700400" MODIFIED="1567364670622" TEXT="when realisng the move keys during a jump, the move impulse is not stopped correctly">
<font NAME="SansSerif" SIZE="14"/>
</node>
</node>
</node>
</node>
</map>
