package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.SyncAlt
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.AppLanguage
import com.example.domain.KrisFlyerTier
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.KrisChampagne
import com.example.ui.theme.KrisGold
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.theme.TierBlueEnd
import com.example.ui.theme.TierBlueStart
import com.example.ui.theme.TierGoldEnd
import com.example.ui.theme.TierGoldStart
import com.example.ui.theme.TierPpsEnd
import com.example.ui.theme.TierPpsStart
import com.example.ui.theme.TierSilverEnd
import com.example.ui.theme.TierSilverStart
import com.example.ui.theme.TierSolitaireEnd
import com.example.ui.theme.TierSolitaireStart
import com.example.ui.viewmodel.formatNumber
import kotlin.math.abs

@Composable
fun KrisFlyerMembershipCard(
    memberName: String,
    membershipNumber: String,
    tier: KrisFlyerTier,
    krisFlyerMiles: Int,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    var flipped by remember { mutableStateOf(false) }
    val rotationY by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = spring(stiffness = 280f, dampingRatio = 0.8f),
        label = "card_flip"
    )

    val (startColor, endColor, accentColor) = when (tier) {
        KrisFlyerTier.KRISFLYER -> Triple(TierBlueStart, TierBlueEnd, KrisChampagne)
        KrisFlyerTier.ELITE_SILVER -> Triple(TierSilverStart, TierSilverEnd, Color.White)
        KrisFlyerTier.ELITE_GOLD -> Triple(TierGoldStart, TierGoldEnd, Color(0xFFFFF5D6))
        KrisFlyerTier.PPS_CLUB -> Triple(TierPpsStart, TierPpsEnd, Color(0xFFF3C6A5))
        KrisFlyerTier.SOLITAIRE_PPS -> Triple(TierSolitaireStart, TierSolitaireEnd, KrisChampagne)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(214.dp)
            .graphicsLayer {
                this.rotationY = rotationY
                cameraDistance = 14f * density
            }
            .clip(RoundedCornerShape(22.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(startColor, endColor, startColor.copy(alpha = 0.92f)),
                    start = Offset(0f, 0f),
                    end = Offset(1000f, 650f)
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(accentColor.copy(alpha = 0.7f), Color.White.copy(alpha = 0.15f))
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { flipped = !flipped }
            .testTag("krisflyer_membership_card")
    ) {
        // Subtle Singapore Airlines Batik geometric arcs on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val circleColor = accentColor.copy(alpha = 0.08f)
            drawCircle(
                color = circleColor,
                radius = size.height * 0.85f,
                center = Offset(size.width * 0.92f, size.height * 0.15f)
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.05f),
                radius = size.height * 0.58f,
                center = Offset(size.width * 0.92f, size.height * 0.15f)
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.04f),
                radius = size.height * 0.70f,
                center = Offset(size.width * 0.08f, size.height * 1.05f)
            )
        }

        if (rotationY <= 90f) {
            // Front Face
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Rounded.Flight,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SINGAPORE AIRLINES",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                letterSpacing = 1.6.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = tier.localizedTitle(language),
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 21.sp,
                            color = accentColor
                        )
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(999.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.QrCode2,
                                contentDescription = if (language == AppLanguage.VI) "Lật thẻ xem mã QR" else "Flip for QR",
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = tier.shortBadge,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column {
                    Text(
                        text = if (language == AppLanguage.VI) "SỐ DẶM KRISFLYER KHẢ DỤNG" else "AVAILABLE KRISFLYER MILES",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.1.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formatNumber(krisFlyerMiles),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = Color.White
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = memberName,
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "KF $membershipNumber",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 13.sp,
                            color = accentColor.copy(alpha = 0.95f)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.SyncAlt,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.VI) "Chạm để lật" else "Tap to flip",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            // Back Face (Digital Lounge Pass & Barcode)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { this.rotationY = 180f }
                    .padding(20.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = tier.starAllianceStatus.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = accentColor,
                            letterSpacing = 1.1.sp
                        )
                    }
                    Text(
                        text = if (language == AppLanguage.VI) "THẺ ĐIỆN TỬ MÔ PHỎNG" else "SIMULATED DIGITAL PASS",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }

                // Simulated high-contrast barcode strip
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SimulatedBarcode(seed = membershipNumber, modifier = Modifier
                            .fillMaxWidth()
                            .height(34.dp))
                        Text(
                            text = "SQ*${membershipNumber.replace(" ", "")}*${tier.shortBadge}",
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp,
                            color = Color(0xFF0B1C3A),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = tier.localizedLounge(language),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun SimulatedBarcode(seed: String, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val bars = 46
        val step = size.width / bars
        for (i in 0 until bars) {
            val charCode = seed[i % seed.length].code + i * 7
            val isThick = (abs(charCode) % 3) == 0
            val barWidth = if (isThick) step * 0.72f else step * 0.38f
            val x = i * step
            drawRect(
                color = Color(0xFF0B1C3A),
                topLeft = Offset(x, 0f),
                size = androidx.compose.ui.geometry.Size(barWidth, size.height)
            )
        }
    }
}
