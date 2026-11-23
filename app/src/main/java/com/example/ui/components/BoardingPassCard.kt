package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.FlightTakeoff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.AwardBookingDoc
import com.example.domain.AppLanguage
import com.example.ui.theme.BatikTeal
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.KrisGold
import com.example.ui.theme.SiaMidnightNavy
import com.example.ui.viewmodel.formatNumber

@Composable
fun BoardingPassCard(
    booking: AwardBookingDoc,
    language: AppLanguage,
    onAdvanceStatus: () -> Unit,
    onCancelRefund: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusLabel, statusBg, statusFg) = when (booking.status) {
        "CONFIRMED" -> Triple(
            if (language == AppLanguage.VI) "ĐÃ XÁC NHẬN" else "CONFIRMED",
            BatikTeal.copy(alpha = 0.14f),
            BatikTeal
        )
        "WAITLIST" -> Triple(
            if (language == AppLanguage.VI) "DANH SÁCH CHỜ" else "WAITLISTED",
            KrisGold.copy(alpha = 0.18f),
            Color(0xFF8C6209)
        )
        "CHECKED_IN" -> Triple(
            if (language == AppLanguage.VI) "ĐÃ CHECK-IN" else "CHECKED IN",
            EmeraldSuccess.copy(alpha = 0.15f),
            EmeraldSuccess
        )
        "COMPLETED" -> Triple(
            if (language == AppLanguage.VI) "ĐÃ HOÀN THÀNH" else "COMPLETED",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        else -> Triple(
            if (language == AppLanguage.VI) "ĐÃ HỦY / HOÀN DẶM" else "CANCELLED",
            CrimsonAccent.copy(alpha = 0.13f),
            CrimsonAccent
        )
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Navy Boarding Pass Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SiaMidnightNavy)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.FlightTakeoff,
                        contentDescription = null,
                        tint = KrisGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${booking.flightNumber} • ${booking.cabinLabel}",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White
                    )
                }
                Text(
                    text = "PNR ${booking.pnr}",
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = KrisGold
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = booking.originCode,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = booking.originCity,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (booking.roundTrip) "⇄" else "→",
                            fontSize = 20.sp,
                            color = KrisGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = booking.awardTypeLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = booking.destinationCode,
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = booking.destinationCity,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Perforated divider effect
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 6.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PassMetaItem(
                        label = if (language == AppLanguage.VI) "NGÀY BAY" else "DATE",
                        value = "${booking.departureDateLabel} • ${booking.departureTimeLabel}"
                    )
                    PassMetaItem(
                        label = if (language == AppLanguage.VI) "CỬA" else "GATE",
                        value = booking.gate
                    )
                    PassMetaItem(
                        label = if (language == AppLanguage.VI) "GHẾ" else "SEAT",
                        value = booking.seatNumber
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusFg,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Text(
                        text = "-${formatNumber(booking.milesCost)} mi • S$${booking.taxesSgd}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (booking.status == "WAITLIST" || booking.status == "CONFIRMED" || booking.status == "CHECKED_IN") {
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val actionText = when (booking.status) {
                            "WAITLIST" -> if (language == AppLanguage.VI) "Xác nhận chỗ" else "Clear Waitlist"
                            "CONFIRMED" -> if (language == AppLanguage.VI) "Check-in" else "Check-in"
                            else -> if (language == AppLanguage.VI) "Hoàn thành bay" else "Complete Flight"
                        }
                        Button(
                            onClick = onAdvanceStatus,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("advance_booking_${booking.pnr}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(actionText, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = onCancelRefund,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cancel_booking_${booking.pnr}")
                        ) {
                            Text(
                                text = if (language == AppLanguage.VI) "Hủy & Hoàn dặm" else "Cancel & Refund",
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PassMetaItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontFamily = JetBrainsMonoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
