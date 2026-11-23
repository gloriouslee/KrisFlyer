package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AirplaneTicket
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.AwardBookingDoc
import com.example.data.remote.MileageTransactionDoc
import com.example.domain.AppLanguage
import com.example.ui.components.BoardingPassCard
import com.example.ui.theme.BatikTeal
import com.example.ui.theme.CrimsonAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.KrisGold
import com.example.ui.theme.PlayfairDisplayFontFamily
import com.example.ui.viewmodel.KrisFlyerUiState
import com.example.ui.viewmodel.formatNumber
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ActivityScreen(
    uiState: KrisFlyerUiState,
    onAdvanceBookingStatus: (AwardBookingDoc) -> Unit,
    onCancelAndRefundBooking: (AwardBookingDoc) -> Unit,
    modifier: Modifier = Modifier
) {
    val lang = uiState.language
    var selectedTab by remember { mutableIntStateOf(0) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredTransactions = remember(uiState.transactions, selectedFilter) {
        if (selectedFilter == "ALL") uiState.transactions
        else uiState.transactions.filter { it.category == selectedFilter }
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column {
                        Text(
                            text = if (lang == AppLanguage.VI) "HOẠT ĐỘNG & VÉ MÔ PHỎNG" else "TRIPS & MILEAGE STATEMENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = KrisGold,
                            letterSpacing = 1.4.sp
                        )
                        Text(
                            text = if (lang == AppLanguage.VI) "Vé Thưởng & Sao Kê" else "Bookings & Ledger",
                            fontFamily = PlayfairDisplayFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            modifier = Modifier.testTag("activity_tab_bookings")
                        ) {
                            Text(
                                text = if (lang == AppLanguage.VI) "Vé Thưởng (${uiState.bookings.size})"
                                else "Boarding Passes (${uiState.bookings.size})"
                            )
                        }
                        SegmentedButton(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            modifier = Modifier.testTag("activity_tab_ledger")
                        ) {
                            Text(
                                text = if (lang == AppLanguage.VI) "Sao Kê (${uiState.transactions.size})"
                                else "Statement (${uiState.transactions.size})"
                            )
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                if (uiState.bookings.isEmpty()) {
                    item {
                        EmptyActivityState(
                            icon = Icons.Rounded.AirplaneTicket,
                            title = if (lang == AppLanguage.VI) "Chưa có vé thưởng mô phỏng" else "No simulated award bookings",
                            body = if (lang == AppLanguage.VI) {
                                "Hãy vào mục Đổi Thưởng để đổi dặm lấy vé Saver hoặc Spontaneous Escapes."
                            } else {
                                "Visit the Redeem tab to issue a simulated Saver or Spontaneous Escapes boarding pass."
                            }
                        )
                    }
                } else {
                    items(uiState.bookings, key = { it.id }) { booking ->
                        BoardingPassCard(
                            booking = booking,
                            language = lang,
                            onAdvanceStatus = { onAdvanceBookingStatus(booking) },
                            onCancelRefund = { onCancelAndRefundBooking(booking) }
                        )
                    }
                }
            } else {
                item {
                    val filters = listOf(
                        "ALL" to (if (lang == AppLanguage.VI) "Tất cả" else "All"),
                        "FLIGHT" to (if (lang == AppLanguage.VI) "Chuyến bay" else "Flights"),
                        "PARTNER" to (if (lang == AppLanguage.VI) "Đối tác" else "Partners"),
                        "REDEMPTION" to (if (lang == AppLanguage.VI) "Đổi thưởng" else "Redemptions"),
                        "REFUND" to (if (lang == AppLanguage.VI) "Hoàn dặm" else "Refunds")
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(filters) { (key, label) ->
                            FilterChip(
                                selected = selectedFilter == key,
                                onClick = { selectedFilter = key },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                if (filteredTransactions.isEmpty()) {
                    item {
                        EmptyActivityState(
                            icon = Icons.Rounded.ReceiptLong,
                            title = if (lang == AppLanguage.VI) "Chưa có giao dịch trong mục này" else "No transactions in this filter",
                            body = if (lang == AppLanguage.VI) {
                                "Thực hiện mô phỏng chuyến bay hoặc tích dặm đối tác để xem lịch sử sao kê tại đây."
                            } else {
                                "Simulate a flight or partner earn to populate your mileage statement."
                            }
                        )
                    }
                } else {
                    items(filteredTransactions, key = { it.id }) { tx ->
                        TransactionRowCard(tx = tx, language = lang)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun TransactionRowCard(
    tx: MileageTransactionDoc,
    language: AppLanguage
) {
    val isPositive = tx.krisFlyerMilesDelta >= 0L
    val deltaColor = if (isPositive) EmeraldSuccess else CrimsonAccent
    val displayTs = tx.createdAt ?: Timestamp.now()
    val formattedDate = remember(displayTs) {
        SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.getDefault()).format(displayTs.toDate())
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tx.badgeCode.take(5),
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (language == AppLanguage.VI) tx.titleVi else tx.titleEn,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == AppLanguage.VI) tx.subtitleVi else tx.subtitleEn,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isPositive) "+${formatNumber(tx.krisFlyerMilesDelta)}" else formatNumber(tx.krisFlyerMilesDelta),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = deltaColor
                )
                if (tx.eliteMilesDelta > 0L) {
                    Text(
                        text = "+${formatNumber(tx.eliteMilesDelta)} EM",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = KrisGold
                    )
                }
                if (tx.ppsValueDeltaSgd > 0L) {
                    Text(
                        text = "+S$${formatNumber(tx.ppsValueDeltaSgd)} PPS",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        color = BatikTeal
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyActivityState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    body: String
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = KrisGold,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
