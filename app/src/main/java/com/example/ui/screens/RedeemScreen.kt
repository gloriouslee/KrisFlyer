package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.rounded.AirlineSeatReclineExtra
import androidx.compose.material.icons.rounded.ConfirmationNumber
import androidx.compose.material.icons.rounded.LocalOffer
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Upgrade
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.Airport
import com.example.domain.AppLanguage
import com.example.domain.AwardCabin
import com.example.domain.AwardType
import com.example.ui.components.AirportSelectorSheet
import com.example.ui.theme.BatikTeal
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.KrisChampagne
import com.example.ui.theme.KrisGold
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.SiaMidnightNavy
import com.example.ui.viewmodel.KrisFlyerUiState
import com.example.ui.viewmodel.formatNumber

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RedeemScreen(
    uiState: KrisFlyerUiState,
    onUpdateRoute: (Airport?, Airport?) -> Unit,
    onSwapAirports: () -> Unit,
    onSelectCabin: (AwardCabin) -> Unit,
    onSelectAwardType: (AwardType) -> Unit,
    onToggleRoundTrip: (Boolean) -> Unit,
    onRedeemAwardFlight: () -> Unit,
    onRedeemLifestyleVoucher: (String, String, String, String, String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = uiState.language
    val profile = uiState.profile
    val balance = (profile?.krisFlyerMiles ?: 0L).toInt()
    val redeemState = uiState.redeemState
    val quote = uiState.awardQuote
    val canAfford = balance >= quote.milesRequired

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
            // Destination / Spontaneous Escapes Banner
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(148.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Image(
                            painter = painterResource(id = R.drawable.img_changi_jewel),
                            contentDescription = if (lang == AppLanguage.VI) "Ưu đãi Spontaneous Escapes" else "Spontaneous Escapes Promo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            SiaMidnightNavy.copy(alpha = 0.92f),
                                            SiaMidnightNavy.copy(alpha = 0.35f)
                                        )
                                    )
                                )
                        )
                        Column(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                color = KrisGold,
                                shape = RoundedCornerShape(999.dp)
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.VI) "SPONTANEOUS ESCAPES -30%" else "SPONTANEOUS ESCAPES -30%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = SiaMidnightNavy,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = if (lang == AppLanguage.VI) "Đổi Vé Thưởng & Nâng Hạng" else "Award Chart & Redemption",
                                fontFamily = PlayfairDisplayFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (lang == AppLanguage.VI) {
                                    "Số dư khả dụng: ${formatNumber(balance)} dặm KrisFlyer"
                                } else {
                                    "Available balance: ${formatNumber(balance)} KrisFlyer miles"
                                },
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 12.sp,
                                color = KrisChampagne
                            )
                        }
                    }
                }
            }

            // Award Flight Search & Simulator Card
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
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AirportPickerBox(
                                label = if (lang == AppLanguage.VI) "KHỞI HÀNH" else "FROM",
                                airport = redeemState.origin,
                                language = lang,
                                onClick = { pickingOrigin = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("redeem_origin_picker")
                            )
                            IconButton(
                                onClick = onSwapAirports,
                                modifier = Modifier
                                    .padding(horizontal = 6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapHoriz,
                                    contentDescription = if (lang == AppLanguage.VI) "Đổi chiều" else "Swap",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            AirportPickerBox(
                                label = if (lang == AppLanguage.VI) "ĐIỂM ĐẾN" else "TO",
                                airport = redeemState.destination,
                                language = lang,
                                onClick = { pickingDestination = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("redeem_destination_picker")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (lang == AppLanguage.VI) "Hành trình" else "Trip Type",
                                style = MaterialTheme.typography.titleSmall
                            )
                            SingleChoiceSegmentedButtonRow {
                                SegmentedButton(
                                    selected = !redeemState.isRoundTrip,
                                    onClick = { onToggleRoundTrip(false) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                                ) {
                                    Text(if (lang == AppLanguage.VI) "1 chiều" else "One-way", fontSize = 12.sp)
                                }
                                SegmentedButton(
                                    selected = redeemState.isRoundTrip,
                                    onClick = { onToggleRoundTrip(true) },
                                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                                ) {
                                    Text(if (lang == AppLanguage.VI) "Khứ hồi" else "Round-trip", fontSize = 12.sp)
                                }
                            }
                        }

                        Text(
                            text = if (lang == AppLanguage.VI) "Chọn Hạng Khoang" else "Select Cabin Class",
                            style = MaterialTheme.typography.titleSmall
                        )

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AwardCabin.entries.forEach { cabin ->
                                FilterChip(
                                    selected = cabin == redeemState.cabin,
                                    onClick = { onSelectCabin(cabin) },
                                    label = { Text(cabin.localizedTitle(lang), fontSize = 12.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.testTag("award_cabin_${cabin.name}")
                                )
                            }
                        }

                        Text(
                            text = if (lang == AppLanguage.VI) "Loại Vé Thưởng" else "Award Tier",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            AwardType.entries.forEach { awardType ->
                                val selected = awardType == redeemState.awardType
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) KrisGold else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onSelectAwardType(awardType) }
                                        .testTag("award_type_${awardType.name}")
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(
                                            text = awardType.localizedTitle(lang),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = awardType.localizedDesc(lang),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Award Quote & Issue Ticket Card
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
                            Column {
                                Text(
                                    text = if (lang == AppLanguage.VI) "SỐ DẶM CẦN ĐỔI" else "MILES REQUIRED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KrisChampagne
                                )
                                Text(
                                    text = "${formatNumber(quote.milesRequired)} mi",
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 26.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (lang == AppLanguage.VI) {
                                        "Thuế & phụ phí sân bay: S$${quote.taxesSgd}"
                                    } else {
                                        "Est. taxes & fees: S$${quote.taxesSgd}"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.72f)
                                )
                            }

                            Surface(
                                color = if (quote.instantConfirmationLikely) BatikTeal.copy(alpha = 0.25f)
                                else KrisGold.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.End
                                ) {
                                    Text(
                                        text = if (quote.instantConfirmationLikely) {
                                            if (lang == AppLanguage.VI) "XÁC NHẬN NGAY" else "INSTANT CONFIRM"
                                        } else {
                                            if (lang == AppLanguage.VI) "DANH SÁCH CHỜ" else "WAITLIST LIKELY"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (quote.instantConfirmationLikely) Color(0xFF77F2EC) else KrisChampagne,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${quote.availabilityRatePercent}% odds",
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }

                        if (!canAfford) {
                            Surface(
                                color = CrimsonAccent.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.VI) {
                                        "Thiếu ${formatNumber(quote.milesRequired - balance)} dặm KrisFlyer so với số dư hiện tại."
                                    } else {
                                        "Need ${formatNumber(quote.milesRequired - balance)} more KrisFlyer miles."
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFFFB4BC),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }

                        Button(
                            onClick = onRedeemAwardFlight,
                            enabled = canAfford,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("redeem_award_flight_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KrisGold,
                                contentColor = SiaMidnightNavy,
                                disabledContainerColor = Color.White.copy(alpha = 0.16f),
                                disabledContentColor = Color.White.copy(alpha = 0.45f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ConfirmationNumber,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (lang == AppLanguage.VI) "Đổi Vé Thưởng & Xuất Thẻ Lên Tàu" else "Book Award & Issue Boarding Pass",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Upgrades & Kris+ / KrisShop Quick Redemptions
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = if (lang == AppLanguage.VI) "Nâng Hạng & Voucher Đời Sống" else "Upgrades & Lifestyle Rewards",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    LifestyleRedeemRow(
                        icon = Icons.Rounded.Upgrade,
                        title = if (lang == AppLanguage.VI) "Nâng hạng Phổ thông → Thương gia (Đông Nam Á)" else "Cabin Upgrade: Economy → Business (SE Asia)",
                        subtitle = if (lang == AppLanguage.VI) "Áp dụng cho vé Standard / Flexi" else "Valid on Standard / Flexi paid fares",
                        milesCost = 12_500,
                        canAfford = balance >= 12_500,
                        language = lang,
                        onRedeem = {
                            onRedeemLifestyleVoucher(
                                "Nâng hạng Thương gia 1 chặng",
                                "One-Way Business Cabin Upgrade",
                                "Nâng từ Phổ thông lên Thương gia (SGN/SIN)",
                                "Upgraded Economy to Business cabin",
                                "UPG",
                                12_500
                            )
                        }
                    )

                    LifestyleRedeemRow(
                        icon = Icons.Rounded.LocalOffer,
                        title = "Kris+ Voucher S$100",
                        subtitle = if (lang == AppLanguage.VI) "Chi tiêu ẩm thực & mua sắm tại Singapore" else "Dining & retail credit across Singapore",
                        milesCost = 10_000,
                        canAfford = balance >= 10_000,
                        language = lang,
                        onRedeem = {
                            onRedeemLifestyleVoucher(
                                "Đổi Voucher Kris+ S$100",
                                "Kris+ S$100 Partner Voucher",
                                "Sử dụng tại đối tác Kris+ Singapore",
                                "Redeemable across Kris+ merchants",
                                "KRIS+",
                                10_000
                            )
                        }
                    )

                    LifestyleRedeemRow(
                        icon = Icons.Rounded.ShoppingBag,
                        title = "KrisShop Duty-Free Credit S$150",
                        subtitle = if (lang == AppLanguage.VI) "Mua sắm đồng hồ, nước hoa & mô hình máy bay SQ" else "Watches, fragrances & SQ aircraft models",
                        milesCost = 15_000,
                        canAfford = balance >= 15_000,
                        language = lang,
                        onRedeem = {
                            onRedeemLifestyleVoucher(
                                "Đổi Voucher KrisShop S$150",
                                "KrisShop S$150 Duty-Free Credit",
                                "Giao tận ghế bay hoặc tại nhà",
                                "In-flight or home delivery order",
                                "SHOP",
                                15_000
                            )
                        }
                    )

                    LifestyleRedeemRow(
                        icon = Icons.Rounded.AirlineSeatReclineExtra,
                        title = if (lang == AppLanguage.VI) "Vé vào Phòng chờ SilverKris (Changi)" else "SilverKris Lounge Single Entry Pass",
                        subtitle = if (lang == AppLanguage.VI) "Thư giãn & ẩm thực cao cấp trước chuyến bay" else "Premium dining & showers at Changi T3",
                        milesCost = 6_000,
                        canAfford = balance >= 6_000,
                        language = lang,
                        onRedeem = {
                            onRedeemLifestyleVoucher(
                                "Vé vào SilverKris Lounge",
                                "SilverKris Lounge Entry Pass",
                                "Sử dụng 1 lượt tại Changi Airport Terminal 3",
                                "Single entry at Changi Airport Terminal 3",
                                "LOUNGE",
                                6_000
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
            selectedCode = redeemState.origin.code,
            language = lang,
            onSelect = { onUpdateRoute(it, null) },
            onDismiss = { pickingOrigin = false }
        )
    }

    if (pickingDestination) {
        AirportSelectorSheet(
            title = if (lang == AppLanguage.VI) "Chọn Sân bay Điểm đến" else "Select Destination Airport",
            selectedCode = redeemState.destination.code,
            language = lang,
            onSelect = { onUpdateRoute(null, it) },
            onDismiss = { pickingDestination = false }
        )
    }
}

@Composable
private fun LifestyleRedeemRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    milesCost: Int,
    canAfford: Boolean,
    language: AppLanguage,
    onRedeem: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
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
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
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
            Button(
                onClick = onRedeem,
                enabled = canAfford,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                modifier = Modifier.testTag("lifestyle_redeem_$milesCost")
            ) {
                Text(
                    text = if (language == AppLanguage.VI) "${formatNumber(milesCost)} dặm" else "${formatNumber(milesCost)} mi",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 11.sp
                )
            }
        }
    }
}
