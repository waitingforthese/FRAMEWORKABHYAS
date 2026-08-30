package com.mahaesuvidha.chandrapanchangalarm

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mahaesuvidha.chandrapanchangalarm.model.AaradhanaMaster
import com.mahaesuvidha.chandrapanchangalarm.alarm.AlarmScheduler
import com.mahaesuvidha.chandrapanchangalarm.model.BirthProfile
import com.mahaesuvidha.chandrapanchangalarm.model.LiveMoonCalculator
import com.mahaesuvidha.chandrapanchangalarm.model.PanchangState
import com.mahaesuvidha.chandrapanchangalarm.settings.AaradhanaPrefs

@Composable
fun AaradhanaScreen(
    profile: BirthProfile,
    panchang: PanchangState,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { AaradhanaPrefs(context.applicationContext) }
    var special by remember { mutableStateOf(prefs.specialHourly) }
    val moon = remember { LiveMoonCalculator.getCurrentMoonState() }
    val nakInfo = AaradhanaMaster.forNakshatra(moon.nakshatra.marathi)
    val yogaInfo = AaradhanaMaster.forYoga(panchang.yoga)
    val karanaInfo = AaradhanaMaster.forKarana(panchang.karana)

    Column(
        Modifier.fillMaxSize().background(Color(0xFF07111F)).verticalScroll(rememberScrollState()).padding(14.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("🕉️ नक्षत्र आराधना", color = Color(0xFFFFC83D), fontSize = 22.sp, fontWeight = FontWeight.Bold)
            TextButton(onClick = onBack) { Text("परत", color = Color.White) }
        }
        Spacer(Modifier.height(10.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10253A))) {
            Column(Modifier.padding(14.dp)) {
                Text("👤 ${profile.name}", color = Color.White, fontWeight = FontWeight.Bold)
                Text("जन्म नक्षत्र: ${profile.birthNakshatra.ifBlank { "—" }}", color = Color.LightGray)
                Text("चंद्र नक्षत्र: ${moon.nakshatra.marathi} • चरण ${moon.pada}", color = Color.LightGray)
                Text("गोचर नक्षत्र: ${moon.nakshatra.marathi}", color = Color.LightGray)
                Text("योग: ${panchang.yoga}", color = Color.LightGray)
                Text("करण: ${panchang.karana}", color = Color.LightGray)
            }
        }
        Spacer(Modifier.height(10.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10253A))) {
            Column(Modifier.padding(14.dp)) {
                Text("🔔 बदलाची आराधना", color = Color(0xFFFFC83D), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("नक्षत्र / योग / करण बदलल्यावर संबंधित देवता व मंत्रासह ११ जप आपोआप होतील.", color = Color.LightGray, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))
                MantraRow("🌟 नक्षत्र", moon.nakshatra.marathi, nakInfo.deity, nakInfo.mantra)
                MantraRow("🕉️ योग", panchang.yoga, yogaInfo.deity, yogaInfo.mantra)
                MantraRow("🔱 करण", panchang.karana, karanaInfo.deity, karanaInfo.mantra)
            }
        }
        Spacer(Modifier.height(10.dp))
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF10253A))) {
            Column(Modifier.padding(14.dp)) {
                Text("🕉️ विशेष आराधना", color = Color(0xFFFFC83D), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("प्रत्येक १ तासाने", color = Color.White, modifier = Modifier.weight(1f))
                    Switch(checked = special, onCheckedChange = { special = it; prefs.specialHourly = it; AlarmScheduler(context.applicationContext).scheduleAll() })
                }
                Text("ON असल्यास: नक्षत्र मंत्र ११ → योग मंत्र ११ → करण मंत्र ११", color = Color.LightGray, fontSize = 13.sp)
                Text("या विशेष आराधनेत कोणतीही घोषणा केली जाणार नाही.", color = Color.LightGray, fontSize = 12.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Text("📖 मार्गदर्शन स्वतंत्र Part मध्ये राहील.", color = Color.LightGray, fontSize = 13.sp)
    }
}

@Composable
private fun MantraRow(label: String, value: String, deity: String, mantra: String) {
    Column(Modifier.padding(vertical = 5.dp)) {
        Text("$label: $value", color = Color.White, fontWeight = FontWeight.Bold)
        Text("🙏 अधिदेवता: $deity", color = Color(0xFFFFC83D), fontSize = 13.sp)
        Text("📿 $mantra", color = Color.White, fontSize = 13.sp)
    }
}
