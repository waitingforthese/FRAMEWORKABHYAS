package com.mahaesuvidha.chandrapanchangalarm

import android.location.Geocoder
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahaesuvidha.chandrapanchangalarm.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private enum class FrameworkKind(val title: String, val icon: String, val subtitle: String) {
    MEDICAL("Medical Astrology", "🩺", "आरोग्याशी संबंधित पारंपरिक ज्योतिषीय संकेतांचा अभ्यास"),
    BUSINESS("Business Astrology", "💼", "व्यवसाय, पैसा, भागीदारी व लाभाचा अभ्यास"),
    EDUCATION("Educational Astrology", "📖", "शिक्षण, बुद्धी, एकाग्रता व उच्च शिक्षणाचा अभ्यास"),
    VASTU("Vastushastra", "🏠", "दिशा, वास्तु घटक व कुंडलीशी तुलनात्मक अभ्यास")
}

private data class FrameworkDay(
    val date: LocalDate,
    val rashi: String,
    val house: Int,
    val nakshatra: String,
    val pada: Int,
    val aspects: String,
    val topic: String,
    val change: String
)

private data class FrameworkPlanet(
    val graha: Graha,
    val birthHouse: Int,
    val birthRashi: String,
    val transit: FrameworkDay,
    val subject: String,
    val houseMeaning: String,
    val reasoning: String,
    val prediction: String,
    val comparison: List<FrameworkDay>
)

@Composable
fun FrameworkScreen(profile: BirthProfile, onBack: () -> Unit) {
    var selected by remember { mutableStateOf<FrameworkKind?>(null) }
    BackHandler { if (selected != null) selected = null else onBack() }
    if (selected == null) {
        FrameworkHome(onBack = onBack, onSelect = { selected = it })
    } else {
        FrameworkDetail(profile, selected!!, onBack = { selected = null })
    }
}

@Composable
private fun FrameworkHome(onBack: () -> Unit, onSelect: (FrameworkKind) -> Unit) {
    Column(Modifier.fillMaxSize().background(Color(0xFF07111F)).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← मागे", color = Color.White) }
            Text("🧠 Framework", color = Color(0xFFFFC83D), fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            Spacer(Modifier.width(55.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text("ग्रहस्थिती → कारण → परिणाम → तुलना → अभ्यास", color = Color.LightGray, fontSize = 13.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        FrameworkKind.entries.forEach { kind ->
            Card(Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { onSelect(kind) }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF10253A)), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFC83D).copy(alpha = .35f))) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(kind.icon, fontSize = 30.sp); Spacer(Modifier.width(12.dp)); Column(Modifier.weight(1f)) { Text(kind.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold); Text(kind.subtitle, color = Color.LightGray, fontSize = 11.sp) }; Text("›", color = Color(0xFFFFC83D), fontSize = 28.sp)
                }
            }
        }
    }
}

@Composable
private fun FrameworkDetail(profile: BirthProfile, kind: FrameworkKind, onBack: () -> Unit) {
    val context = LocalContext.current
    var coords by remember(profile.birthPlace) { mutableStateOf<Pair<Double, Double>?>(null) }
    LaunchedEffect(profile.birthPlace) { coords = withContext(Dispatchers.IO) { runCatching { if (!Geocoder.isPresent()) null else Geocoder(context, java.util.Locale.getDefault()).getFromLocationName(profile.birthPlace, 1)?.firstOrNull()?.let { it.latitude to it.longitude } }.getOrNull() } }
    val data = remember(profile, coords, kind) { if (coords == null) emptyList() else FrameworkCalculator.calculate(profile, coords!!.first, coords!!.second, kind) }
    Column(Modifier.fillMaxSize().background(Color(0xFF07111F)).statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(12.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { TextButton(onClick = onBack) { Text("← मागे", color = Color.White) }; Text("${kind.icon} ${kind.title}", color = Color(0xFFFFC83D), fontSize = 19.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center); Spacer(Modifier.width(55.dp)) }
        Spacer(Modifier.height(6.dp))
        Text("जन्मकुंडलीतील भाव = जन्मलग्नापासून  •  गोचर भाव = जन्म चंद्रराशीपासून", color = Color.LightGray, fontSize = 11.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        if (coords == null) Text("जन्मठिकाणाचे coordinates शोधत आहे...", color = Color.Gray, modifier = Modifier.padding(12.dp))
        if (kind == FrameworkKind.VASTU) VastuInfo()
        data.forEach { planet -> PlanetStudyCard(planet, kind) }
        if (data.isEmpty() && coords != null) Text("विश्लेषणासाठी जन्ममाहिती तपासा.", color = Color.LightGray, modifier = Modifier.padding(16.dp))
    }
}

@Composable
private fun PlanetStudyCard(p: FrameworkPlanet, kind: FrameworkKind) {
    var open by remember { mutableStateOf(false) }
    var compare by remember { mutableStateOf(false) }
    var logic by remember { mutableStateOf(false) }
    val card = Color(0xFF10253A); val gold = Color(0xFFFFC83D); val white = Color(0xFFF5F7FA)
    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = card)) {
        Column(Modifier.padding(14.dp)) {
            Row(Modifier.fillMaxWidth().clickable { open = !open }, verticalAlignment = Alignment.CenterVertically) { Text("${planetEmoji(p.graha.marathi)} ${p.graha.marathi}", color = white, fontSize = 17.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f)); Text(if (open) "⌃" else "⌄", color = gold, fontSize = 22.sp) }
            Text("आज: ${p.transit.house}वा भाव • ${p.transit.rashi} • ${p.transit.nakshatra}", color = gold, fontSize = 12.sp)
            if (open) {
                Spacer(Modifier.height(8.dp))
                StudyLabel("गोचर ग्रह कोणता?", p.graha.marathi)
                StudyLabel("संबंधित विषय", p.subject)
                StudyLabel("कोणत्या भावातून गोचर करतो?", "${p.transit.house}वा भाव — ${p.houseMeaning}")
                StudyLabel("गोचर राशी", p.transit.rashi)
                StudyLabel("जन्मकुंडलीतील ग्रह", "${p.birthHouse}वा भाव — ${p.birthRashi}")
                StudyLabel("दृष्टी", p.transit.aspects)
                StudyLabel("नक्षत्र / चरण", "${p.transit.nakshatra} / ${p.transit.pada}")
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { logic = !logic }, modifier = Modifier.weight(1f)) { Text("🔍 भाकीत कसे?", fontSize = 11.sp) }
                    OutlinedButton(onClick = { compare = !compare }, modifier = Modifier.weight(1f)) { Text("📊 Comparison", fontSize = 11.sp) }
                }
                if (logic) {
                    Spacer(Modifier.height(8.dp)); Text("🧠 हे भाकीत कसे तयार झाले?", color = gold, fontWeight = FontWeight.Bold)
                    Text(p.reasoning, color = white, fontSize = 13.sp, modifier = Modifier.padding(top = 5.dp))
                    Spacer(Modifier.height(6.dp)); Text("🔮 भाकीत", color = gold, fontWeight = FontWeight.Bold); Text(p.prediction, color = white, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
                }
                if (compare) ComparisonTable(p.comparison)
            }
        }
    }
}

@Composable private fun StudyLabel(label: String, value: String) { Column(Modifier.padding(vertical = 3.dp)) { Text(label, color = Color(0xFFFFC83D), fontSize = 11.sp, fontWeight = FontWeight.Bold); Text(value, color = Color(0xFFF5F7FA), fontSize = 13.sp) } }

@Composable private fun ComparisonTable(rows: List<FrameworkDay>) {
    Spacer(Modifier.height(10.dp)); Text("📊 मागील 2 दिवस • आज • पुढील 2 दिवस", color = Color(0xFFFFC83D), fontWeight = FontWeight.Bold)
    rows.forEachIndexed { i, r ->
        val label = when (i) { 0 -> "-2"; 1 -> "-1"; 2 -> "आज"; 3 -> "+1"; else -> "+2" }
        Card(Modifier.fillMaxWidth().padding(vertical = 2.dp), colors = CardDefaults.cardColors(containerColor = if (i == 2) Color(0xFF1A344D) else Color(0xFF0C1D2D))) {
            Column(Modifier.padding(8.dp)) { Text("$label  •  ${r.date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp); Text("${r.house}वा भाव • ${r.rashi} • ${r.nakshatra} (चरण ${r.pada})", color = Color(0xFFF5F7FA), fontSize = 11.sp); Text("दृष्टी: ${r.aspects}", color = Color.LightGray, fontSize = 10.sp); Text("विषय: ${r.topic}", color = Color.LightGray, fontSize = 10.sp); Text("बदल: ${r.change}", color = Color(0xFFFFC83D), fontSize = 10.sp) }
        }
    }
}

@Composable private fun VastuInfo() { Card(Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color(0xFF10253A))) { Column(Modifier.padding(14.dp)) { Text("🏠 स्थिर वास्तु अभ्यास", color = Color(0xFFFFC83D), fontWeight = FontWeight.Bold); Text("दिशा, पंचमहाभूत, वास्तुपुरुष मंडल, ईशान्य, आग्नेय, नैऋत्य, वायव्य, ब्रह्मस्थान, मुख्य प्रवेश, kitchen, bedroom, पूजा/ध्यान, office आणि धनस्थान यांचा स्वतंत्र अभ्यास करा. वास्तुचे स्थिर नियम आणि दैनिक ग्रहगोचर एकच गोष्ट म्हणून दाखवू नयेत.", color = Color.White, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp)) } } }

private object FrameworkCalculator {
    private val bodies = listOf(Graha.SURYA to swisseph.SweConst.SE_SUN, Graha.CHANDRA to swisseph.SweConst.SE_MOON, Graha.MANGAL to swisseph.SweConst.SE_MARS, Graha.BUDH to swisseph.SweConst.SE_MERCURY, Graha.GURU to swisseph.SweConst.SE_JUPITER, Graha.SHUKRA to swisseph.SweConst.SE_VENUS, Graha.SHANI to swisseph.SweConst.SE_SATURN, Graha.RAHU to swisseph.SweConst.SE_TRUE_NODE)
    fun calculate(profile: BirthProfile, lat: Double, lon: Double, kind: FrameworkKind): List<FrameworkPlanet> {
        val birth = BirthChartCalculator.calculate(profile.birthDate, profile.birthTime, lat, lon)
        val moonIndex = Rashi.entries.indexOfFirst { it.marathi == profile.birthMoonRashi }.let { if (it >= 0) it else 0 }
        val today = LocalDate.now()
        val allBodies = bodies + (Graha.KETU to -1)
        return allBodies.map { (g, body) ->
            val bp = birth[g] ?: BirthChartCalculator.PlanetPosition(0, 1)
            val days = (-2..2).map { offset -> day(profile, lat, lon, moonIndex, g, body, today.plusDays(offset.toLong()), offset, kind) }
            val now = days[2]
            val subject = subjects[g] ?: "ग्रहाशी संबंधित पारंपरिक विषय"
            FrameworkPlanet(g, bp.house, Rashi.entries[bp.rashiIndex].marathi, now, subject, houseMeaning(now.house, kind), reasoning(g, bp.house, Rashi.entries[bp.rashiIndex].marathi, now, kind), prediction(g, now, kind), days)
        }
    }
    private fun day(profile: BirthProfile, lat: Double, lon: Double, moonIndex: Int, g: Graha, body: Int, date: LocalDate, offset: Int, kind: FrameworkKind): FrameworkDay {
        val jd = julianDay(date, 12.0)
        val swe = swisseph.SwissEph().apply { swe_set_sid_mode(swisseph.SweConst.SE_SIDM_LAHIRI, 0.0, 0.0) }
        val rawLongitude = if (body == -1) (longitude(swe, jd, swisseph.SweConst.SE_TRUE_NODE) + 180.0) % 360.0 else longitude(swe, jd, body)
        val idx = rashiIndex(rawLongitude); val house = (idx - moonIndex + 12) % 12 + 1
        val nak = Nakshatra.entries[(rawLongitude / (360.0 / 27.0)).toInt().coerceIn(0, 26)]
        val pada = (((rawLongitude % (360.0 / 27.0)) / (360.0 / 108.0)).toInt() + 1).coerceIn(1,4)
        val aspects = aspectText(g, house)
        val topic = houseMeaning(house, kind)
        val change = if (offset == 0) "आजची आधारस्थिती" else "आजच्या तुलनेत ${if (house == ((idx - moonIndex + 12) % 12 + 1)) "भावस्थिती कायम" else "भाव बदल"}"
        return FrameworkDay(date, Rashi.entries[idx].marathi, house, nak.marathi, pada, aspects, topic, change)
    }
    private fun longitude(swe: swisseph.SwissEph, jd: Double, body: Int): Double { val xx=DoubleArray(6); swe.swe_calc_ut(jd, body, swisseph.SweConst.SEFLG_SWIEPH or swisseph.SweConst.SEFLG_SIDEREAL, xx, StringBuffer()); return ((xx[0] % 360)+360)%360 }
    private fun rashiIndex(v: Double) = (v/30.0).toInt().coerceIn(0,11)
    private fun julianDay(date: LocalDate, hour: Double): Double { val cal=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("Asia/Kolkata")); cal.set(date.year,date.monthValue-1,date.dayOfMonth,12,0,0); cal.set(java.util.Calendar.MILLISECOND,0); return swisseph.SweDate.getJulDay(cal.get(java.util.Calendar.YEAR),cal.get(java.util.Calendar.MONTH)+1,cal.get(java.util.Calendar.DAY_OF_MONTH),hour,swisseph.SweDate.SE_GREG_CAL) }
    private fun aspectText(g: Graha, house: Int): String { val hs = when(g){Graha.MANGAL->listOf(4,7,8); Graha.GURU->listOf(5,7,9); Graha.SHANI->listOf(3,7,10); else->listOf(7)}; return hs.joinToString(", "){ "${((house+it-2)%12)+1}वा भाव" } }
    private fun houseMeaning(h: Int, kind: FrameworkKind) = when(kind){ FrameworkKind.MEDICAL -> mapOf(1 to "शरीर/स्वास्थ्य",2 to "आहार/वाणी",3 to "हात-खांदे/प्रयत्न",4 to "छाती/मन",5 to "पचन/उदर",6 to "रोग/सेवा",7 to "संबंध",8 to "दीर्घकालीन संकेत",9 to "भाग्य/ज्ञान",10 to "कर्म",11 to "लाभ",12 to "विश्रांती/खर्च")[h] ?: "जीवनक्षेत्र"; FrameworkKind.BUSINESS -> mapOf(2 to "पैसा/भांडवल",3 to "मार्केटिंग/संवाद",6 to "स्पर्धा/operations",7 to "भागीदारी/clients",10 to "व्यवसाय/कर्म",11 to "profit/network",12 to "expenses")[h] ?: "व्यवसायाचे जीवनक्षेत्र"; FrameworkKind.EDUCATION -> mapOf(4 to "मूलभूत शिक्षण",5 to "बुद्धी/learning",9 to "उच्च शिक्षण",2 to "आहार/वाणी",3 to "प्रयत्न",10 to "career")[h] ?: "शिक्षणाशी संबंधित क्षेत्र"; FrameworkKind.VASTU -> "दिशा/वास्तुशी तुलनात्मक अभ्यास" }
    private val subjects = mapOf(Graha.SURYA to "आत्मविश्वास, अधिकार, प्रतिष्ठा, नेतृत्व, वरिष्ठ, सरकारी काम", Graha.CHANDRA to "मन, भावना, सवय, संवेदनशीलता, जनसंपर्क", Graha.MANGAL to "ऊर्जा, धाडस, कृती, स्पर्धा, जमीन", Graha.BUDH to "बुद्धी, संवाद, व्यापार, गणित, लेखन", Graha.GURU to "ज्ञान, मार्गदर्शन, विस्तार, शिक्षण, भाग्य", Graha.SHUKRA to "संबंध, सुख, कला, पैसा, सुविधा", Graha.SHANI to "शिस्त, विलंब, कामगार, जबाबदारी, दीर्घकालीन प्रयत्न", Graha.RAHU to "आकांक्षा, परकीय विषय, असामान्य मार्ग, भ्रम", Graha.KETU to "विरक्ती, संशोधन, अंतर्मुखता")
    private fun reasoning(g: Graha, birthHouse: Int, birthRashi: String, now: FrameworkDay, kind: FrameworkKind) = "जन्मकुंडलीतील ${g.marathi} ${birthHouse}व्या भावात $birthRashi राशीत आहे. त्यामुळे जन्मतः ${subjects[g] ?: "संबंधित विषय"} या ग्रहाशी जोडलेले आहेत. सध्या तो जन्म चंद्रराशीपासून ${now.house}व्या भावातून ${now.rashi} राशीत गोचर करतो. त्या भावाचे ${houseMeaning(now.house, kind)} हे क्षेत्र सक्रिय होते. ${now.nakshatra} नक्षत्र, चरण ${now.pada} आणि ग्रहाची दृष्टी विचारात घेऊन परिणामाचा संदर्भ तयार होतो. म्हणून अंतिम भाकीत एकाच घटकावर नाही तर जन्मस्थिती + गोचर भाव + रास + दृष्टी + नक्षत्र यांच्या संयोगावर आधारित आहे."
    private fun prediction(g: Graha, now: FrameworkDay, kind: FrameworkKind) = "${g.marathi} सध्या ${now.house}व्या भावातून गोचर करत असल्याने ${houseMeaning(now.house, kind)} क्षेत्राचा पारंपरिक ज्योतिषीय अभ्यास करण्यासाठी हा दिवस महत्त्वाचा आहे. हा निष्कर्ष अभ्यासात्मक आहे; अंतिम फलितासाठी इतर ग्रह, दशा, नक्षत्र व संपूर्ण कुंडलीचा संदर्भ आवश्यक आहे."
}
