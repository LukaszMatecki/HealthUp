package com.lumatech.voltup

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorsScreen(
    onNavigateBack: () -> Unit
) {
    CommonBackHandler(enabled = true, onBack = onNavigateBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Autorzy Aplikacji",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Wróć"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Groups,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }

                        Text(
                            text = "Ekipa Twórców VoltUp",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Projekt VoltUp powstał z pasji do nowoczesnych technologii, aktywności fizycznej oraz zrównoważonego rozwoju miasta Łódź.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            item {
                Text(
                    text = "ZESPÓŁ ROZWOJU PROJEKTU",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                )
            }

            val teamMembers = listOf(
                TeamMember(
                    role = "Lead Mobile Architect & Developer",
                    name = "Główny Inżynier Aplikacji",
                    description = "Projekt i implementacja architektury w Kotlin Multiplatform, interfejs w Jetpack Compose, obsługa czujników stacji i optymalizacja wydajności.",
                    icon = Icons.Default.Code,
                    color = Color(0xFF10B981)
                ),
                TeamMember(
                    role = "Cloud & Backend Architect",
                    name = "Inżynier Bazy Danych i Chmury",
                    description = "Architektura Google Firebase i Cloud Firestore, system autoryzacji, mechanizmy synchronizacji offline oraz bezpieczeństwo danych użytkowników.",
                    icon = Icons.Default.CloudQueue,
                    color = Color(0xFF3B82F6)
                ),
                TeamMember(
                    role = "UI/UX & Product Designer",
                    name = "Projektant Interfejsu i Doświadczeń",
                    description = "Projekt systemu wzornictwa VoltUp, dobór typografii i palety kolorów, ergonomia ekranu treningowego oraz animacje interaktywne.",
                    icon = Icons.Default.Palette,
                    color = Color(0xFF8B5CF6)
                ),
                TeamMember(
                    role = "Kinetic IoT & Firmware Specialist",
                    name = "Inżynier Prądnic i Aparatury Miejskej",
                    description = "Integracja bezszczotkowych generatorów prądu, modułów inwerterowych w parkowych rowerach oraz kalibracja pomiaru energii w watogodzinach.",
                    icon = Icons.Default.Sensors,
                    color = Color(0xFFF59E0B)
                )
            )

            items(teamMembers.size) { index ->
                MemberCard(member = teamMembers[index])
            }

            item {
                Text(
                    text = "PARTNERZY I INSTYTUCJE MIEJSKIE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp, top = 12.dp)
                )
            }

            val partners = listOf(
                PartnerInfo(
                    name = "Urząd Miasta Łodzi",
                    role = "Departament Rewitalizacji i Ekologii",
                    detail = "Wsparcie merytoryczne i realizacja programu zielonej energii miejskiej w Łodzi."
                ),
                PartnerInfo(
                    name = "MPK-Łódź Sp. z o.o.",
                    role = "Oficjalny Partner Nagród Miejskich",
                    detail = "Darmowe bilety komunikacji miejskiej wymieniane za punkty kinetyczne VoltPoints."
                ),
                PartnerInfo(
                    name = "Zarząd Zieleni Miejskiej w Łodzi",
                    role = "Gospodarz Stacji Parkowych",
                    detail = "Lokalizacje w Parku Poniatowskiego, na Zdrowiu, w Parku Źródliska oraz przy Stawach Stefańskiego."
                ),
                PartnerInfo(
                    name = "EC1 Łódź – Miasto Kultury",
                    role = "Partner Edukacji Energetycznej",
                    detail = "Wspólne warsztaty o odnawialnych źródłach energii i zrównoważonym transporcie."
                )
            )

            items(partners.size) { index ->
                PartnerCard(partner = partners[index])
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "© 2026 VoltUp Smart City Łódź • Wszelkie prawa zastrzeżone",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                )
            }
        }
    }
}

private data class TeamMember(
    val role: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: Color
)

@Composable
private fun MemberCard(member: TeamMember) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(member.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = member.icon,
                    contentDescription = null,
                    tint = member.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.role,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = member.name,
                    style = MaterialTheme.typography.labelSmall,
                    color = member.color,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = member.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

private data class PartnerInfo(
    val name: String,
    val role: String,
    val detail: String
)

@Composable
private fun PartnerCard(partner: PartnerInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = partner.name,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = partner.role,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = partner.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}
