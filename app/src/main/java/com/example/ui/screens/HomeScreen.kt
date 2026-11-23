package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AirlineSeatReclineExtra
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Luggage
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.repository.TierPreset
import com.example.domain.AppLanguage
import com.example.domain.KrisFlyerTier
import com.example.domain.MilestoneRewardsCatalog
import com.example.ui.components.KrisFlyerMembershipCard
import com.example.ui.theme.BatikTeal
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.KrisChampagne
import com.example.ui.theme.KrisGold
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.SiaMidnightNavy
import com.example.ui.viewmodel.KrisFlyerUiState
import com.example.ui.viewmodel.formatNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: KrisFlyerUiState,
    isDarkTheme: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleDarkMode: () -> Unit,
    onApplyPreset: (TierPreset) -> Unit,
    onUpdateMemberName: (String) -> Unit,
    onNavigateToEarn: () -> Unit,
    onNavigateToRedeem: () -> Unit,
    onDismissCelebration: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val profile = uiState.profile ?: return
    val lang = uiState.language
    val tier = uiState.currentTier
    var showSimulatorSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (lang == AppLanguage.VI) "GIẢ LẬP KRISFLYER" else "KRISFLYER SIMULATOR",
                            style = MaterialTheme.typography.labelSmall,
                            color = KrisGold,
                            letterSpacing = 1.4.sp
                        )
                        Text(
                            text = if (lang == AppLanguage.VI) "Singapore Airlines" else "Singapore Airlines",
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(999.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { onToggleLanguage() }
                                .testTag("language_toggle_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Language,
                                    contentDescription = if (lang == AppLanguage.VI) "Đổi ngôn ngữ" else "Switch language",
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (lang == AppLanguage.VI) "VI" else "EN",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        IconButton(
                            onClick = onToggleDarkMode,
                            modifier = Modifier.testTag("theme_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = if (lang == AppLanguage.VI) "Đổi giao diện sáng/tối" else "Toggle theme"
                            )
                        }

                        IconButton(
                            onClick = { showSimulatorSheet = true },
                            modifier = Modifier.testTag("simulator_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = if (lang == AppLanguage.VI) "Cấu hình giả lập" else "Simulator settings"
                            )
                        }
                    }
                }
            }

            // Interactive 3D Flippable Membership Card
            item {
                KrisFlyerMembershipCard(
                    memberName = profile.memberName,
                    membershipNumber = profile.membershipNumber,
                    tier = tier,
                    krisFlyerMiles = profile.krisFlyerMiles.toInt(),
                    language = lang
                )
            }

            // Quick Action Buttons
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToEarn,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("home_go_earn_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.FlightTakeoff,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == AppLanguage.VI) "Mô phỏng Bay" else "Simulate Flight",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    FilledTonalButton(
                        onClick = onNavigateToRedeem,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("home_go_redeem_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CardGiftcard,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (lang == AppLanguage.VI) "Đổi Vé Thưởng" else "Redeem Miles",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Elite Miles & PPS Value Dual Progress Cards
            item {
                Text(
                    text = if (lang == AppLanguage.VI) "Tiến Độ Nâng Hạng (12 Tháng)" else "Tier Qualification Progress",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val eliteTarget = if (profile.eliteMiles < 25_000) 25_000 else 50_000
                    val eliteTargetName = if (profile.eliteMiles < 25_000) "Elite Silver" else "Elite Gold"
                    TierProgressCard(
                        title = "Elite Miles",
                        currentFormatted = formatNumber(profile.eliteMiles),
                        targetFormatted = formatNumber(eliteTarget),
                        nextTierLabel = if (profile.eliteMiles >= 50_000) {
                            if (lang == AppLanguage.VI) "Đã đạt Elite Gold" else "Elite Gold Reached"
                        } else {
                            if (lang == AppLanguage.VI) "Mục tiêu: $eliteTargetName" else "Goal: $eliteTargetName"
                        },
                        progress = (profile.eliteMiles.toFloat() / eliteTarget.toFloat()).coerceIn(0f, 1f),
                        accentColor = KrisGold,
                        modifier = Modifier.weight(1f)
                    )

                    val ppsTarget = if (profile.ppsValueSgd < 25_000) 25_000 else 50_000
                    val ppsTargetName = if (profile.ppsValueSgd < 25_000) "PPS Club" else "Solitaire PPS"
                    TierProgressCard(
                        title = "PPS Value",
                        currentFormatted = "S$${formatNumber(profile.ppsValueSgd)}",
                        targetFormatted = "S$${formatNumber(ppsTarget)}",
                        nextTierLabel = if (profile.ppsValueSgd >= 50_000) {
                            if (lang == AppLanguage.VI) "Đã đạt Solitaire PPS" else "Solitaire Reached"
                        } else {
                            if (lang == AppLanguage.VI) "Mục tiêu: $ppsTargetName" else "Goal: $ppsTargetName"
                        },
                        progress = (profile.ppsValueSgd.toFloat() / ppsTarget.toFloat()).coerceIn(0f, 1f),
                        accentColor = BatikTeal,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Expiring Miles Notice
            if (profile.expiringMiles > 0) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (lang == AppLanguage.VI) "Dặm sắp hết hạn (Mô phỏng)" else "Expiring Miles (Simulated)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (lang == AppLanguage.VI) {
                                        "${formatNumber(profile.expiringMiles)} dặm hết hạn vào ${profile.expiringDateLabel}"
                                    } else {
                                        "${formatNumber(profile.expiringMiles)} miles expiring ${profile.expiringDateLabel}"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            TextButton(onClick = onNavigateToRedeem) {
                                Text(if (lang == AppLanguage.VI) "Dùng ngay" else "Use now")
                            }
                        }
                    }
                }
            }

            // Milestone Rewards Track
            item {
                Column {
                    Text(
                        text = if (lang == AppLanguage.VI) "KrisFlyer Milestone Rewards" else "KrisFlyer Milestone Rewards",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (lang == AppLanguage.VI) {
                            "Mở khóa đặc quyền tự động khi tích lũy Elite Miles trên các chuyến bay SQ"
                        } else {
                            "Unlock perks automatically as you earn Elite Miles on Singapore Airlines"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(MilestoneRewardsCatalog.items) { reward ->
                            val unlocked = profile.eliteMiles >= reward.thresholdEliteMiles
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (unlocked) MaterialTheme.colorScheme.secondaryContainer
                                else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (unlocked) KrisGold else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.width(176.dp)
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${formatNumber(reward.thresholdEliteMiles)} EM",
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (unlocked) MaterialTheme.colorScheme.onSecondaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Icon(
                                            imageVector = if (unlocked) Icons.Rounded.Verified else Icons.Rounded.Lock,
                                            contentDescription = null,
                                            tint = if (unlocked) KrisGold else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = if (lang == AppLanguage.VI) reward.titleVi else reward.titleEn,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        minLines = 2,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tier Privileges & SilverKris Lounge Showcase Card
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    tonalElevation = 2.dp
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(156.dp)
                        ) {
                            Image(
                                painter = painterResource(
                                    id = if (tier >= KrisFlyerTier.PPS_CLUB) R.drawable.img_sq_cabin_hero
                                    else R.drawable.img_silverkris_lounge
                                ),
                                contentDescription = if (lang == AppLanguage.VI) "Phòng chờ & Khoang hạng sang Singapore Airlines" else "Singapore Airlines Lounge & Cabin",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                SiaMidnightNavy.copy(alpha = 0.88f)
                                            )
                                        )
                                    )
                            )
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = if (lang == AppLanguage.VI) "ĐẶC QUYỀN HẠNG HIỆN TẠI" else "ACTIVE TIER PRIVILEGES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = KrisChampagne
                                )
                                Text(
                                    text = tier.localizedTitle(lang),
                                    fontFamily = PlayfairDisplayFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PrivilegeRow(
                                icon = Icons.Rounded.AirlineSeatReclineExtra,
                                label = tier.localizedLounge(lang)
                            )
                            PrivilegeRow(
                                icon = Icons.Rounded.Luggage,
                                label = if (lang == AppLanguage.VI) {
                                    "Hành lý ký gửi ưu đãi thêm: +${tier.baggageBonusKg} kg"
                                } else {
                                    "Extra checked baggage allowance: +${tier.baggageBonusKg} kg"
                                }
                            )
                            PrivilegeRow(
                                icon = Icons.Rounded.WorkspacePremium,
                                label = tier.localizedPerks(lang)
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }

    // Simulator Presets & Profile Sheet
    if (showSimulatorSheet) {
        var nameInput by remember(profile.memberName) { mutableStateOf(profile.memberName) }
        ModalBottomSheet(
            onDismissRequest = { showSimulatorSheet = false },
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = if (lang == AppLanguage.VI) "Cài đặt Giả lập & Hồ sơ" else "Simulator Presets & Profile",
                    style = MaterialTheme.typography.headlineSmall
                )
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(if (lang == AppLanguage.VI) "Tên hội viên trên thẻ" else "Cardholder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        TextButton(onClick = { onUpdateMemberName(nameInput) }) {
                            Text(if (lang == AppLanguage.VI) "Lưu" else "Save")
                        }
                    }
                )

                Text(
                    text = if (lang == AppLanguage.VI) "Chuyển nhanh kịch bản hạng thẻ:" else "Quick Tier Presets:",
                    style = MaterialTheme.typography.titleMedium
                )

                PresetRow(
                    title = if (lang == AppLanguage.VI) "Khởi đầu từ 0 (KrisFlyer)" else "Fresh Start (0 Miles)",
                    subtitle = "0 KF • 0 Elite • S$0 PPS",
                    onClick = {
                        onApplyPreset(TierPreset.FRESH_START)
                        showSimulatorSheet = false
                    }
                )
                PresetRow(
                    title = if (lang == AppLanguage.VI) "Gần đạt Elite Silver (Mặc định)" else "Near Elite Silver (Default)",
                    subtitle = "38,450 KF • 18,200 Elite • S$4,800 PPS",
                    onClick = {
                        onApplyPreset(TierPreset.NEAR_SILVER)
                        showSimulatorSheet = false
                    }
                )
                PresetRow(
                    title = "KrisFlyer Elite Gold (Star Gold)",
                    subtitle = "86,500 KF • 54,200 Elite • S$11,400 PPS",
                    onClick = {
                        onApplyPreset(TierPreset.ELITE_GOLD)
                        showSimulatorSheet = false
                    }
                )
                PresetRow(
                    title = "PPS Club (SilverKris Business Lounge)",
                    subtitle = "168,000 KF • 62,000 Elite • S$29,500 PPS",
                    onClick = {
                        onApplyPreset(TierPreset.PPS_CLUB)
                        showSimulatorSheet = false
                    }
                )
                PresetRow(
                    title = "Solitaire PPS Club (First Class Lounge)",
                    subtitle = "345,000 KF • 95,000 Elite • S$56,800 PPS",
                    onClick = {
                        onApplyPreset(TierPreset.SOLITAIRE_PPS)
                        showSimulatorSheet = false
                    }
                )

                if (!uiState.userEmail.isNullOrBlank()) {
                    Text(
                        text = if (lang == AppLanguage.VI) "Đang đăng nhập: ${uiState.userEmail}"
                        else "Signed in as ${uiState.userEmail}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = {
                        showSimulatorSheet = false
                        onSignOut()
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (lang == AppLanguage.VI) "Đăng xuất tài khoản Google" else "Sign out of Google")
                }
            }
        }
    }

    // Tier Upgrade Celebration Dialog
    uiState.celebratedUpgradeTier?.let { upgradedTier ->
        AlertDialog(
            onDismissRequest = onDismissCelebration,
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(KrisGold.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = KrisGold,
                        modifier = Modifier.size(30.dp)
                    )
                }
            },
            title = {
                Text(
                    text = if (lang == AppLanguage.VI) "Chúc mừng Thăng hạng!" else "Tier Upgrade Unlocked!",
                    fontFamily = PlayfairDisplayFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (lang == AppLanguage.VI) {
                            "Bạn vừa đạt hạng ${upgradedTier.titleVi} (${upgradedTier.starAllianceStatus})!"
                        } else {
                            "You have qualified for ${upgradedTier.titleEn} (${upgradedTier.starAllianceStatus})!"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = upgradedTier.localizedLounge(lang),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(onClick = onDismissCelebration) {
                    Text(if (lang == AppLanguage.VI) "Tuyệt vời" else "Awesome")
                }
            }
        )
    }
}

@Composable
private fun TierProgressCard(
    title: String,
    currentFormatted: String,
    targetFormatted: String,
    nextTierLabel: String,
    progress: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 650),
        label = "tier_progress"
    )
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = currentFormatted,
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "/ $targetFormatted",
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = nextTierLabel,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PrivilegeRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = KrisGold,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PresetRow(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            Text(
                text = subtitle,
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
