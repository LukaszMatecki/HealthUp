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
    var selectedTab by remember { mutableStateOf(HelpTab.FAQ) }
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Pomoc i Instrukcja",
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
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                    .clickable { selectedTab = tab }
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tab.title,
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
                                text = "Jak działa VoltUp?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "4 proste kroki do zielonej energii w Łodzi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))

                    GuideStepRow("1", "Zlokalizuj stację", "Otwórz mapę i wybierz dowolny rower lub orbitrek w łódzkich parkach.")
                    GuideStepRow("2", "Zeskanuj kod QR", "Kliknij przycisk skanera QR i wyceluj w kod umieszczony na ramie stacji.")
                    GuideStepRow("3", "Ćwicz i twórz prąd", "Pedałuj lub ruszaj się! Każdy ruch wytwarza watogodziny (Wh) i redukuje CO₂.")
                    GuideStepRow("4", "Odbieraj nagrody", "Wymieniaj zgromadzone VoltPoints na darmowe bilety MPK-Łódź i kupony partnerów.")
                }
            }
        }

        item {
            Text(
                text = "CZĘSTO ZADAWANE PYTANIA (FAQ)",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp)
            )
        }

        val faqItems = listOf(
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
                            text = "Skontaktuj się z działem pomocy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Masz pytanie dotyczące konta, biletów MPK lub sposobu naliczania VoltPoints? Nasz łódzki zespół wsparcia chętnie pomoże!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }

                    Button(
                        onClick = {
                            try {
                                uriHandler.openUri("mailto:pomoc@voltup.pl?subject=VoltUp%20-%20Pytanie%20do%20dzialu%20pomocy")
                            } catch (_: Exception) {
                                SnackbarHelper.show("Napisz do nas: pomoc@voltup.pl")
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
                        Text("Napisz do nas (pomoc@voltup.pl)", fontWeight = FontWeight.Bold)
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
                            text = "Zgłoś błąd lub awarię stacji",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Zauważyłeś uszkodzoną stację w parku, niedziałający opór prądnicy lub błąd w aplikacji? Zgłoś usterkę, a technicy niezwłocznie się nią zajmą.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }

                    Button(
                        onClick = {
                            try {
                                uriHandler.openUri("mailto:bledy@voltup.pl?subject=VoltUp%20-%20Zgloszenie%20awarii%20lub%20bledu")
                            } catch (_: Exception) {
                                SnackbarHelper.show("Zgłoś błąd na: bledy@voltup.pl")
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
                        Text("Zgłoś awarię (bledy@voltup.pl)", fontWeight = FontWeight.Bold, color = Color.White)
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Biuro Projektu VoltUp",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Departament Rewitalizacji i Ekologii Urzędu Miasta Łodzi\nul. Piotrkowska 104, 90-926 Łódź\nInfolinia czynna pn-pt w godz. 8:00 - 16:00",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
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
