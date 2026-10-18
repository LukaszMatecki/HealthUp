package com.lumatech.voltup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class HelpTab(val title: String) {
    FAQ("Instrukcja & FAQ"),
    SUPPORT("Dział Pomocy & Kontakt")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpGuideScreen(
    onNavigateBack: () -> Unit
) {
    CommonBackHandler(enabled = true, onBack = onNavigateBack)
    val strings = strings
    var selectedTab by remember { mutableStateOf(HelpTab.FAQ) }
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = strings.helpGuideTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = strings.back
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Segmented Tab Selector (podobny do zakładki Nagrody)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        HelpTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            val tabLabel = when (tab) {
                                HelpTab.FAQ -> strings.helpTabFaq
                                HelpTab.SUPPORT -> strings.helpTabSupport
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedTab = tab }
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tabLabel,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            when (selectedTab) {
                HelpTab.FAQ -> FaqTabContent()
                HelpTab.SUPPORT -> SupportTabContent(uriHandler = uriHandler)
            }
        }
    }
}

@Composable
private fun FaqTabContent() {
    val isEnglish = strings is VoltUpEnglishStrings
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isEnglish) "How does VoltUp work?" else "Jak działa VoltUp?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEnglish) "4 simple steps to green energy in Łódź" else "4 proste kroki do zielonej energii w Łodzi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    GuideStepRow(
                        "1",
                        if (isEnglish) "Locate a station" else "Zlokalizuj stację",
                        if (isEnglish) "Open the map and pick any kinetic bike or cross trainer in Łódź parks." else "Otwórz mapę i wybierz dowolny rower lub orbitrek w łódzkich parkach."
                    )
                    GuideStepRow(
                        "2",
                        if (isEnglish) "Scan QR code" else "Zeskanuj kod QR",
                        if (isEnglish) "Tap the QR scanner and aim at the code placed on the equipment frame." else "Kliknij przycisk skanera QR i wyceluj w kod umieszczony na ramie stacji."
                    )
                    GuideStepRow(
                        "3",
                        if (isEnglish) "Exercise & generate power" else "Ćwicz i twórz prąd",
                        if (isEnglish) "Pedal or exercise! Every movement produces watt-hours (Wh) and saves CO₂." else "Pedałuj lub ruszaj się! Każdy ruch wytwarza watogodziny (Wh) i redukuje CO₂."
                    )
                    GuideStepRow(
                        "4",
                        if (isEnglish) "Claim rewards" else "Odbieraj nagrody",
                        if (isEnglish) "Redeem your VoltPoints for free MPK-Łódź transit tickets and partner coupons." else "Wymieniaj zgromadzone VoltPoints na darmowe bilety MPK-Łódź i kupony partnerów."
                    )
                }
            }
        }

        item {
            Text(
                text = if (isEnglish) "FREQUENTLY ASKED QUESTIONS (FAQ)" else "CZĘSTO ZADAWANE PYTANIA (FAQ)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        val faqItems = if (isEnglish) listOf(
            FaqItemData(
                question = "How do kinetic stations power the city of Łódź?",
                answer = "Every VoltUp outdoor gym station contains a brushless kinetic generator and micro-inverter. The electricity generated during your workout flows directly to illuminate park pathways (such as in Poniatowski Park or Zdrowie Park) and public USB chargers."
            ),
            FaqItemData(
                question = "What is the difference between VoltPoints and ranking points?",
                answer = "Ranking points represent your overall level, achievements, and leaderboard rank. VoltPoints are your spending wallet currency used to redeem free MPK-Łódź transit tickets and merchant discounts."
            ),
            FaqItemData(
                question = "How do I activate an MPK-Łódź ticket purchased with points?",
                answer = "Go to 'Rewards' -> 'My Coupons'. Tap 'Tap to activate' right when boarding the tram or bus. An official validation code and a 20-minute countdown clock will appear for the ticket controller."
            ),
            FaqItemData(
                question = "Does the app require GPS to be constantly enabled?",
                answer = "No! Location permission is completely optional. GPS is only used to center the map on your position and show distances to nearby equipment."
            ),
            FaqItemData(
                question = "What should I do if a kinetic machine does not respond to QR code?",
                answer = "Make sure the station has an 'Online' status on the map. You can also start the session directly from the map station list. If the machine is physically damaged, use the 'Help & Support' tab to alert our maintenance technicians."
            )
        ) else listOf(
            FaqItemData(
                question = "Jak stacje kinetyczne zasilają miasto Łódź?",
                answer = "Każda stacja VoltUp wyposażona jest w bezszczotkową prądnicę kinetyczną i mikrofallowy inwerter. Energia elektryczna wygenerowana podczas Twojego treningu trafia bezpośrednio do zasilania oświetlenia parkowych alejek (np. w Parku Poniatowskiego czy na Zdrowiu) oraz ładowarek miejskich."
            ),
            FaqItemData(
                question = "Czym różnią się VoltPoints od punktów w rankingu?",
                answer = "Punkty w rankingu reprezentują Twój łączny dorobek i poziom zaangażowania w VoltUp. VoltPoints to natomiast Twój wirtualny portfel punktów do wydania w sklepie na darmowe bilety komunikacji miejskiej MPK-Łódź i rabaty partnerskie."
            ),
            FaqItemData(
                question = "Jak aktywować bilet MPK-Łódź kupiony za punkty?",
                answer = "Wejdź w zakładkę 'Nagrody' -> 'Moje kupony'. Kliknij 'Aktywuj kupon' w momencie wsiadania do pojazdu. Wyświetli się oficjalny kod z odliczającym zegarem ważności 20 minut, który możesz okazać kontrolerowi biletów MPK."
            ),
            FaqItemData(
                question = "Czy aplikacja wymaga stałego włączonego GPS?",
                answer = "Nie! Korzystanie z lokalizacji jest całkowicie opcjonalne. GPS służy jedynie do wycentrowania mapy na Twojej pozycji i wyświetlania dokładnej odległości do najbliższych stacji."
            ),
            FaqItemData(
                question = "Co zrobić, jeśli stacja kinetyczna nie reaguje na QR kod?",
                answer = "Upewnij się, że stacja na mapie ma status 'Online'. Możesz również wybrać stację bezpośrednio z listy na mapie bez skanowania. Jeśli urządzenie ma usterkę fizyczną, skorzystaj z zakładki 'Dział Pomocy', aby zgłosić awarię naszym serwisantom."
            )
        )

        items(faqItems.size) { index ->
            FaqAccordionCard(faq = faqItems[index])
        }
    }
}

@Composable
private fun SupportTabContent(uriHandler: androidx.compose.ui.platform.UriHandler) {
    val isEnglish = strings is VoltUpEnglishStrings
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
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
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isEnglish) "Contact Support Team" else "Skontaktuj się z działem pomocy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEnglish) "Have a question about your account, MPK tickets, or how VoltPoints are calculated? Our Łódź support team is happy to help!" else "Masz pytanie dotyczące konta, biletów MPK lub sposobu naliczania VoltPoints? Nasz łódzki zespół wsparcia chętnie pomoże!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }

                    Button(
                        onClick = {
                            try {
                                uriHandler.openUri("mailto:help.lumatech@gmail.com?subject=VoltUp%20-%20Support%20Question")
                            } catch (_: Exception) {
                                SnackbarHelper.show(if (isEnglish) "Email us: help.lumatech@gmail.com" else "Napisz do nas: help.lumatech@gmail.com")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isEnglish) "Contact us" else "Napisz do nas", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = null,
                            tint = Color(0xFFEF4444),
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isEnglish) "Report issue or station damage" else "Zgłoś błąd lub awarię stacji",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isEnglish) "Noticed a damaged machine in the park, faulty generator resistance, or an app bug? Submit a report and our technical crew will handle it immediately." else "Zauważyłeś uszkodzoną stację w parku, niedziałający opór prądnicy lub błąd w aplikacji? Zgłoś usterkę, a technicy niezwłocznie się nią zajmą.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }

                    Button(
                        onClick = {
                            try {
                                uriHandler.openUri("mailto:help.lumatech@gmail.com?subject=VoltUp%20-%20Issue%20Report")
                            } catch (_: Exception) {
                                SnackbarHelper.show(if (isEnglish) "Report bug to: help.lumatech@gmail.com" else "Zgłoś błąd na: help.lumatech@gmail.com")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                    ) {
                        Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isEnglish) "Report issue" else "Zgłoś awarię", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun GuideStepRow(step: String, title: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

private data class FaqItemData(val question: String, val answer: String)

@Composable
private fun FaqAccordionCard(faq: FaqItemData) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = faq.answer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
