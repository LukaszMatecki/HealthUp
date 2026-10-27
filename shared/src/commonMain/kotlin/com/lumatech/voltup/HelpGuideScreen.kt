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
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.style.TextAlign
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

            Spacer(modifier = Modifier.height(6.dp))

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
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isEnglish) "How does HealthUp work?" else "Jak działa HealthUp?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEnglish) "Safe movement & health for every Łódź resident" else "Zdrowie i bezpieczny ruch dla każdego łodzianina",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    GuideStepRow(
                        "1",
                        if (isEnglish) "Find a friendly park station" else "Wybierz stację w parku",
                        if (isEnglish) "Check the map for recumbent bikes, steppers or cross-trainers in Łódź parks (e.g. Park na Zdrowiu, Poniatowski Park)." else "Sprawdź mapę i wybierz wygodny rower poziomy, orbitrek lub stepper w łódzkim parku (np. Park na Zdrowiu, Park Poniatowskiego)."
                    )
                    GuideStepRow(
                        "2",
                        if (isEnglish) "Simple start without hassle" else "Wygodny start bez pośpiechu",
                        if (isEnglish) "Scan the QR code on the machine or tap the station directly from the map list." else "Zeskanuj kod QR aparatem lub wybierz stację jednym kliknięciem bezpośrednio z listy na mapie."
                    )
                    GuideStepRow(
                        "3",
                        if (isEnglish) "Move gently at your own pace" else "Ćwicz we własnym tempie",
                        if (isEnglish) "15–20 minutes of mild exercise improves heart health and mobility. The built-in generator measures your calories and minutes." else "Spokojny 15-minutowy ruch dotlenia organizm i poprawia krążenie. Kinetostacja zlicza Twój wysiłek, spalone kalorie i czas."
                    )
                    GuideStepRow(
                        "4",
                        if (isEnglish) "Claim MPK transit tickets & perks" else "Odbieraj darmowe bilety MPK",
                        if (isEnglish) "Exchange earned HealthPoints for free MPK-Łódź public transit tickets, discounts to Fala aquapark, and local cafes." else "Wymieniaj zdobyte HealthPoints na bezpłatne bilety MPK-Łódź, wejściówki do Aquaparku Fala, EC1 i zniżki w lokalnych kawiarniach."
                    )
                }
            }
        }

        item {
            Text(
                text = if (isEnglish) "FREQUENTLY ASKED QUESTIONS (FAQ)" else "CZĘSTO ZADAWANE PYTANIA (FAQ)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 22.dp, bottom = 8.dp)
            )
        }

        val faqItems = if (isEnglish) listOf(
            FaqItemData(
                question = "App Idea",
                answer = "HealthUp was created in Łódź to encourage residents — particularly seniors and people aged 50+ — to adopt regular, safe outdoor physical activity. Over 60% of Łódź citizens do not meet the minimum WHO physical activity recommendations, and outdoor gyms often sit empty due to lack of motivation.\n\nHealthUp gives movement a tangible purpose: kinetic outdoor gym machines measure your minutes and calories, awarding HealthPoints. You can redeem these points for free MPK-Łódź transit tickets and city discounts. While machines generate small amounts of electricity to light the station, the true mission is cardiovascular health, vitality, and mobility for all generations."
            ),
            FaqItemData(
                question = "Workout Safety",
                answer = "Yes, 100%! The kinetic equipment in Łódź parks (such as recumbent cycles with ergonomic back support and low-impact steppers) provides smooth, guided movement without heavy weights or joint strain. You exercise at a relaxed walking pace and can pause or rest at any moment."
            ),
            FaqItemData(
                question = "Training Intensity",
                answer = "Absolutely not! HealthUp rewards consistency and gentle aerobic movement in the heart-health zone, rather than athletic exhaustion. 15 to 20 minutes of relaxed pedaling per day is all you need to steadily accumulate points for transit tickets."
            ),
            FaqItemData(
                question = "Energy & City Power",
                answer = "Physics dictates that an exercising adult generates about 50–100 Watts of electrical power. One hour of effort produces ~0.06 kWh (worth a few cents), which cannot realistically power city districts. In HealthUp, this electricity illuminates the station and powers public USB ports, but its main role is a tamper-proof meter of effort. The real municipal value is citizen health and chronic disease prevention."
            ),
            FaqItemData(
                question = "Free MPK Tickets",
                answer = "Navigate to 'Rewards' and select the MPK-Łódź 20-minute ticket. Tap 'Redeem' and it will be stored in 'My Coupons'. When boarding a bus or tram, tap 'Tap to activate' to reveal your official validation code and a 20-minute countdown timer to present during ticket inspection."
            ),
            FaqItemData(
                question = "Required Equipment",
                answer = "No. Any basic smartphone works. You don't need smartwatches or chest straps — the kinetic station sensors automatically track your RPM, resistance, and duration directly."
            ),
            FaqItemData(
                question = "GPS Location",
                answer = "No! Location permission is completely optional. It is solely used to center the park map on your position and show distances to nearby equipment."
            ),
            FaqItemData(
                question = "Points & Ranking",
                answer = "Ranking points (XP) reflect your long-term engagement and community milestones in Łódź. HealthPoints are your spending balance in the app wallet, used to redeem transit tickets and merchant discounts."
            ),
            FaqItemData(
                question = "QR Code Issues",
                answer = "Check if the station has an 'Online' status on the map. You can also start the session directly from the map list without scanning. If the machine has a mechanical issue, submit a ticket in 'Help & Support'."
            )
        ) else listOf(
            FaqItemData(
                question = "Idea aplikacji",
                answer = "HealthUp powstał w Łodzi, aby odwrócić negatywne trendy zdrowotne i zachęcić mieszkańców – w szczególności osoby w wieku 50+ oraz seniorów – do regularnego, bezpiecznego ruchu na świeżym powietrzu. Ponad 60% łodzian nie osiąga minimalnej normy aktywności fizycznej zalecanej przez WHO, a tradycyjne siłownie plenerowe w parkach często stoją puste z braku motywacji.\n\nHealthUp nadaje ruchowi cel: maszyny wyposażone w małe prądnice zliczają spalone kalorie i czas aktywności, zamieniając je na punkty HealthPoints. Te punkty wymieniasz na darmowe bilety MPK-Łódź, wejściówki do Aquaparku Fala, EC1 czy zniżki na zdrowy obiad. Wygenerowana energia to symboliczny dowód wysiłku i zasilenie stacji – prawdziwym sercem projektu jest Twoje serce, stawy i lepsze samopoczucie!"
            ),
            FaqItemData(
                question = "Bezpieczeństwo ćwiczeń",
                answer = "Tak, w 100%! Maszyny kinetyczne HealthUp (rowerki poziome ze stabilnym oparciem, steppery i orbitreki) gwarantują płynny ruch o niskim oporze. Nie obciążają kręgosłupa ani stawów kolanowych i biodrowych. Każdy ćwiczy we własnym, bezpiecznym tempie spacerowym, mogąc w każdej chwili zrobić przerwę."
            ),
            FaqItemData(
                question = "Intensywność treningu",
                answer = "Absolutnie nie! HealthUp premiuje regularność i spokojną aktywność w strefie zdrowia serca, a nie bicie wyczynowych rekordów. Już 15–20 minut łagodnego pedałowania dziennie wystarczy, by systematycznie gromadzić punkty na bezpłatny bilet MPK."
            ),
            FaqItemData(
                question = "Energia a zasilanie miasta",
                answer = "Z praw fizyki wynika, że człowiek podczas ćwiczeń generuje średnio 50–100 watów mocy. Godzina wysiłku to ok. 0,06 kWh (wartość kilkunastu groszy) – to nie wystarczy na zasilenie miasta. W HealthUp energia napędza lokalne oświetlenie maszyny, ekran LED i ładowarkę USB, ale jej kluczową rolą jest weryfikacja realnego wysiłku. Prawdziwą wartością dla Łodzi jest zdrowsze społeczeństwo i profilaktyka!"
            ),
            FaqItemData(
                question = "Darmowe bilety MPK",
                answer = "W zakładce 'Nagrody' wybierz bilet MPK-Łódź (np. bilet 20-minutowy) i kliknij 'Odbierz bilet'. Zostanie on zapisany w zakładce 'Moje kupony'. Przy wsiadaniu do tramwaju lub autobusu kliknij 'Aktywuj kupon' – wyświetli się kod i oficjalny zegar ważności do okazania kontrolerowi."
            ),
            FaqItemData(
                question = "Wymagany sprzęt",
                answer = "Nie. Wystarczy dowolny smartfon. Nie potrzebujesz opasek fitness ani smartwatchy – sensory wbudowane w maszynę na siłowni same łączą się z Twoim kontem i mierzą Twój ruch."
            ),
            FaqItemData(
                question = "Lokalizacja GPS",
                answer = "Nie! Korzystanie z GPS jest opcjonalne. Służy wyłącznie do tego, by pokazać na mapie najbliższy park i wygodną trasę do urządzeń."
            ),
            FaqItemData(
                question = "HealthPoints a ranking",
                answer = "Punkty w rankingu (XP) odzwierciedlają Twój staż i osiągnięcia w społeczności Łodzi. HealthPoints to Twoja waluta w portfelu – to je wydajesz na bilety MPK i kupony miejskie."
            ),
            FaqItemData(
                question = "Problem z kodem QR",
                answer = "Upewnij się, że stacja na mapie ma status 'Online'. Możesz również uruchomić trening bezpośrednio z listy stacji na mapie (bez użycia aparatu). W przypadku fizycznej usterki sprzętu, zgłoś ją w zakładce 'Dział Pomocy & Kontakt'."
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
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isEnglish) "Contact Support Team" else "Skontaktuj się z działem pomocy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isEnglish) "Have a question about your account, MPK tickets, or how HealthPoints work? Our Łódź support team is happy to help!" else "Masz pytanie o konto, bilety MPK lub naliczanie punktów? Nasz łódzki zespół chętnie pomoże!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }

                    Button(
                        onClick = {
                            try {
                                uriHandler.openUri("mailto:help.lumatech@gmail.com?subject=HealthUp%20-%20Support%20Question")
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
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
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
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = if (isEnglish) "Report issue or station damage" else "Zgłoś błąd lub awarię stacji",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isEnglish) "Damaged machine in the park or app glitch? Let us know and we will fix it promptly." else "Uszkodzona maszyna w parku lub problem w aplikacji? Daj nam znać, a szybko to naprawimy.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }

                    Button(
                        onClick = {
                            try {
                                uriHandler.openUri("mailto:help.lumatech@gmail.com?subject=HealthUp%20-%20Issue%20Report")
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
            .clip(RoundedCornerShape(18.dp))
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = faq.question,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp,
                    modifier = Modifier.weight(1f).padding(end = 8.dp)
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = faq.answer,
                        fontSize = 13.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                    )
                }
            }
        }
    }
}
