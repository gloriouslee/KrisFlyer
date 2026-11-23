package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.LocalMall
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AirlineCarrier
import com.example.domain.Airport
import com.example.domain.AppLanguage
import com.example.domain.CabinFareClass
import com.example.domain.KrisFlyerNetwork
import com.example.ui.components.AirportSelectorSheet
import com.example.ui.theme.BatikTeal
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.KrisChampagne
import com.example.ui.theme.KrisGold
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.SiaMidnightNavy
import com.example.ui.viewmodel.KrisFlyerUiState
import com.example.ui.viewmodel.formatNumber
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EarnScreen(
    uiState: KrisFlyerUiState,
    onSelectCarrier: (AirlineCarrier) -> Unit,
    onUpdateRoute: (Airport?, Airport?) -> Unit,
    onSwapAirports: () -> Unit,
    onSelectCabin: (CabinFareClass) -> Unit,
    onToggleRoundTrip: (Boolean) -> Unit,
    onUpdateFareSgd: (Int) -> Unit,
    onSimulateFlight: () -> Unit,
    onSimulatePartner: (String, String, String, String, String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = uiState.language
    val earnState = uiState.earnState
    val preview = uiState.earnPreview

    var pickingOrigin by remember { mutableStateOf(false) }
    var pickingDestination by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column {
                    Text(
                        text = if (lang == AppLanguage.VI) "BỘ MÔ PHỎNG TÍCH DẶM" else "FLIGHT & PARTNER SIMULATOR",
                        style = MaterialTheme.typography.labelSmall,
                        color = KrisGold,
                        letterSpacing = 1.4.sp
                    )
                    Text(
                        text = if (lang == AppLanguage.VI) "Tính & Cộng Dặm Bay" else "Earn KrisFlyer Miles",
                        fontFamily = PlayfairDisplayFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }

            // Route & Trip Card
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    tonalElevation = 1.dp
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            AirportPickerBox(
                                label = if (lang == AppLanguage.VI) "ĐIỂM ĐI" else "FROM",
                                airport = earnState.origin,
                                language = lang,
                                onClick = { pickingOrigin = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("earn_origin_picker")
                            )

                            IconButton(
                                onClick = onSwapAirports,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .testTag("earn_swap_airports")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapHoriz,
                                    contentDescription = if (lang == AppLanguage.VI) "Đổi chiều bay" else "Swap airports",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            AirportPickerBox(
                                label = if (lang == AppLanguage.VI) "ĐIỂM ĐẾN" else "TO",
                                airport = earnState.destination,
                                language = lang,
                                onClick = { pickingDestination = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("earn_destination_picker")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val singleDist = KrisFlyerNetwork.distanceMiles(earnState.origin, earnState.destination)
                            Text(
                                text = if (lang == AppLanguage.VI) {
                                    "Cự ly bay: ${formatNumber(singleDist)} dặm / chặng"
                                } else {
                                    "Distance: ${formatNumber(singleDist)} mi / leg"
                                },
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            SingleChoiceSegmentedButtonRow {
                                SegmentedButton(
                                    selected = !earnState.isRoundTrip,
                                    onClick = { onToggleRoundTrip(false) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                                ) {
                                    Text(if (lang == AppLanguage.VI) "1 chiều" else "One-way", fontSize = 12.sp)
                                }
                                SegmentedButton(
                                    selected = earnState.isRoundTrip,
                                    onClick = { onToggleRoundTrip(true) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                                ) {
                                    Text(if (lang == AppLanguage.VI) "Khứ hồi" else "Round-trip", fontSize = 12.sp)
                                }
                            }
                        }

                        // Airline Carrier Selector (SQ vs Scoot)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            AirlineCarrier.entries.forEachIndexed { index, carrier ->
                                SegmentedButton(
                                    selected = earnState.carrier == carrier,
                                    onClick = { onSelectCarrier(carrier) },
                                    shape = SegmentedButtonDefaults.itemShape(
                                        index = index,
                                        count = AirlineCarrier.entries.size
                                    ),
                                    modifier = Modifier.testTag("carrier_chip_${carrier.code}")
                                ) {
                                    Text(carrier.localizedTitle(lang), fontSize = 12.sp)
                                }
                            }
                        }

                        if (earnState.carrier == AirlineCarrier.SINGAPORE_AIRLINES) {
                            // Cabin & Fare Family Selector
                            Text(
                                text = if (lang == AppLanguage.VI) "Hạng Khoang & Hạng Vé (Booking Class)" else "Cabin & Fare Family",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CabinFareClass.entries.forEach { fareClass ->
                                    val selected = fareClass == earnState.cabin
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onSelectCabin(fareClass) },
                                        label = {
                                            Text(
                                                text = "${fareClass.localizedTitle(lang)} (${(fareClass.kfRate * 100).roundToInt()}%)",
                                                fontSize = 12.sp
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                        ),
                                        modifier = Modifier.testTag("fare_chip_${fareClass.name}")
                                    )
                                }
                            }
                        } else {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.VI) {
                                        "Quy tắc Scoot (TR): Tích 1 KrisFlyer mile / S$1 giá vé & dịch vụ cộng thêm (+25% cho Elite/PPS) và nhân 2.5x Elite Miles."
                                    } else {
                                        "Scoot (TR) Rule: Earn 1 KrisFlyer mile per S$1 base fare & add-ons (+25% Elite/PPS bonus) and 2.5x Elite Miles per KF mile."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(12.dp)
                                )
                            }
                        }

                        // Ticket Fare SGD Slider for PPS Value / Scoot Spend
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (earnState.carrier == AirlineCarrier.SCOOT) {
                                        if (lang == AppLanguage.VI) "Chi tiêu Scoot (S$)" else "Scoot Fare & Add-ons (SGD)"
                                    } else {
                                        if (lang == AppLanguage.VI) "Giá vé cơ bản (tính PPS Value)" else "Airfare Spend (SGD for PPS Value)"
                                    },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "S$${formatNumber(earnState.ticketFareSgd)}",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (earnState.carrier == AirlineCarrier.SINGAPORE_AIRLINES && earnState.cabin.ppsEligible) BatikTeal
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Slider(
                                value = earnState.ticketFareSgd.toFloat(),
                                onValueChange = { onUpdateFareSgd((it / 50f).roundToInt() * 50) },
                                valueRange = if (earnState.carrier == AirlineCarrier.SCOOT) 50f..3000f else 200f..18000f,
                                colors = SliderDefaults.colors(
                                    thumbColor = KrisGold,
                                    activeTrackColor = KrisGold
                                )
                            )
                            Text(
                                text = when {
                                    earnState.carrier == AirlineCarrier.SCOOT -> {
                                        if (lang == AppLanguage.VI) "Chuyến bay Scoot không cộng PPS Value nhưng tăng tốc 2.5x Elite Miles!"
                                        else "Scoot flights do not earn PPS Value, but accelerate Elite Miles at 2.5x!"
                                    }
                                    earnState.cabin.ppsEligible -> {
                                        if (lang == AppLanguage.VI) "Vé Thương gia / Suites được cộng 100% PPS Value!"
                                        else "Business / Suites fares earn 100% PPS Value!"
                                    }
                                    else -> {
                                        if (lang == AppLanguage.VI) "Chỉ vé Thương gia / Suites mới tích lũy PPS Value"
                                        else "Only Business / First / Suites fares accrue PPS Value"
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (earnState.carrier == AirlineCarrier.SINGAPORE_AIRLINES && earnState.cabin.ppsEligible) BatikTeal
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Live Breakdown Preview & Action Button
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SiaMidnightNavy
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == AppLanguage.VI) "DỰ TÍNH DẶM NHẬN ĐƯỢC" else "ESTIMATED ACCRUAL",
                                style = MaterialTheme.typography.labelSmall,
                                color = KrisChampagne,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = if (earnState.carrier == AirlineCarrier.SCOOT) {
                                    "TR • S$${formatNumber(earnState.ticketFareSgd)}"
                                } else {
                                    "${earnState.cabin.bookingCodes} • ${formatNumber(preview.distanceMiles)} mi"
                                },
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            EarnStatBlock(
                                label = "KrisFlyer Miles",
                                value = "+${formatNumber(preview.totalKrisFlyerMiles)}",
                                sub = when {
                                    preview.tierBonusMiles > 0 -> "+${formatNumber(preview.tierBonusMiles)} tier bonus"
                                    earnState.carrier == AirlineCarrier.SCOOT -> "1 mile / S$1"
                                    else -> "Base ${(earnState.cabin.kfRate * 100).roundToInt()}%"
                                },
                                highlightColor = KrisChampagne
                            )
                            EarnStatBlock(
                                label = "Elite Miles",
                                value = "+${formatNumber(preview.eliteMilesEarned)}",
                                sub = if (earnState.carrier == AirlineCarrier.SCOOT) "2.5x KF miles"
                                else "${(earnState.cabin.eliteRate * 100).roundToInt()}% rate",
                                highlightColor = Color.White
                            )
                            EarnStatBlock(
                                label = "PPS Value",
                                value = "+S$${formatNumber(preview.ppsValueSgd)}",
                                sub = if (earnState.carrier == AirlineCarrier.SINGAPORE_AIRLINES && earnState.cabin.ppsEligible) "Eligible" else "N/A",
                                highlightColor = if (preview.ppsValueSgd > 0) Color(0xFF6EF3EB) else Color.White.copy(alpha = 0.5f)
                            )
                        }

                        Button(
                            onClick = onSimulateFlight,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("simulate_flight_credit_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KrisGold,
                                contentColor = SiaMidnightNavy
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.FlightTakeoff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == AppLanguage.VI) "Mô phỏng Chuyến bay & Cộng Dặm" else "Simulate & Credit Flight",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Partner & Everyday Earn Quick Simulators
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (lang == AppLanguage.VI) "Tích Dặm Đối Tác & Đời Sống" else "Everyday & Partner Earn",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    PartnerEarnRow(
                        icon = Icons.Rounded.CreditCard,
                        title = if (lang == AppLanguage.VI) "Chuyển điểm Thẻ Tín dụng Ngân hàng" else "Bank Credit Card Points Transfer",
                        subtitle = if (lang == AppLanguage.VI) "Quy đổi 25.000 điểm ngân hàng → +10.000 dặm" else "Convert bank rewards → +10,000 KF miles",
                        miles = 10_000,
                        onSimulate = {
                            onSimulatePartner(
                                "Chuyển điểm Thẻ Tín dụng",
                                "Bank Points Conversion",
                                "Đối tác tài chính • Cộng tức thì",
                                "Financial partner instant transfer",
                                "BANK",
                                10_000
                            )
                        }
                    )

                    PartnerEarnRow(
                        icon = Icons.Rounded.Restaurant,
                        title = "Kris+ Lifestyle & Dining",
                        subtitle = if (lang == AppLanguage.VI) "Thanh toán tại Singapore & đối tác (+1.500 dặm)" else "Kris+ partner spend (+1,500 miles)",
                        miles = 1_500,
                        onSimulate = {
                            onSimulatePartner(
                                "Kris+ Dining & Pay",
                                "Kris+ Dining & Pay",
                                "Tích lũy 9 dặm / S$1 chi tiêu",
                                "Earned 9 miles per S$1 spent",
                                "KRIS+",
                                1_500
                            )
                        }
                    )

                    PartnerEarnRow(
                        icon = Icons.Rounded.TravelExplore,
                        title = "Pelago Experiences",
                        subtitle = if (lang == AppLanguage.VI) "Đặt vé tham quan & trải nghiệm du lịch (+2.400 dặm)" else "Tours & attractions booking (+2,400 miles)",
                        miles = 2_400,
                        onSimulate = {
                            onSimulatePartner(
                                "Pelago by Singapore Airlines",
                                "Pelago by Singapore Airlines",
                                "Đặt trải nghiệm du lịch • 3 dặm / S$1",
                                "Travel experience booking • 3 mpd",
                                "PELAGO",
                                2_400
                            )
                        }
                    )

                    PartnerEarnRow(
                        icon = Icons.Rounded.LocalMall,
                        title = "KrisShop Duty-Free",
                        subtitle = if (lang == AppLanguage.VI) "Mua sắm miễn thuế giao tận ghế bay (+3.500 dặm)" else "In-flight duty-free shopping (+3,500 miles)",
                        miles = 3_500,
                        onSimulate = {
                            onSimulatePartner(
                                "KrisShop Duty-Free",
                                "KrisShop Duty-Free",
                                "Mua sắm miễn thuế • +3.500 dặm",
                                "Duty-free order • +3,500 miles",
                                "SHOP",
                                3_500
                            )
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    if (pickingOrigin) {
        AirportSelectorSheet(
            title = if (lang == AppLanguage.VI) "Chọn Sân bay Khởi hành" else "Select Origin Airport",
            selectedCode = earnState.origin.code,
            language = lang,
            onSelect = { onUpdateRoute(it, null) },
            onDismiss = { pickingOrigin = false }
        )
    }

    if (pickingDestination) {
        AirportSelectorSheet(
            title = if (lang == AppLanguage.VI) "Chọn Sân bay Điểm đến" else "Select Destination Airport",
            selectedCode = earnState.destination.code,
            language = lang,
            onSelect = { onUpdateRoute(null, it) },
            onDismiss = { pickingDestination = false }
        )
    }
}

@Composable
fun AirportPickerBox(
    label: String,
    airport: Airport,
    language: AppLanguage,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = airport.code,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = airport.localizedCity(language),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun EarnStatBlock(
    label: String,
    value: String,
    sub: String,
    highlightColor: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = highlightColor
        )
        Text(
            text = sub,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.65f)
        )
    }
}

@Composable
private fun PartnerEarnRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    miles: Int,
    onSimulate: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onSimulate)
            .testTag("partner_earn_$miles")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.AddCircle,
                contentDescription = "Simulate +$miles",
                tint = KrisGold,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
