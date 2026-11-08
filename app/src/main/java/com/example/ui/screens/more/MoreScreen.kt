package com.example.ui.screens.more

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.navigation.Screen
import com.example.ui.theme.AppBg
import com.example.ui.theme.CardBg
import com.example.ui.theme.DeepNavy
import com.example.ui.theme.LightGrayDivider
import com.example.ui.theme.NeutralTextSecondary
import com.example.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(AppBg)
            .statusBarsPadding(),
        containerColor = AppBg,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "المزيد",
                        color = DeepNavy,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppBg)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Banner Profile Header
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(22.dp)),
                    shape = RoundedCornerShape(22.dp),
                    color = CardBg
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(
                                text = "تطبيق حساباتي المالي",
                                color = DeepNavy,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "الإصدار 1.0 • إدارة محاسبية متكاملة",
                                color = NeutralTextSecondary,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(PrimaryBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Section 1: الإعدادات المالية
            item {
                SectionGroup(title = "الإعدادات المالية") {
                    MoreMenuItem(
                        icon = Icons.Default.AttachMoney,
                        iconBg = Color(0xFFEAF2FF),
                        iconColor = PrimaryBlue,
                        title = "إدارة العملات",
                        subtitle = "تحديد العملة الأساسية وأسعار الصرف",
                        onClick = { onNavigate(Screen.Currencies.route) }
                    )
                    MoreMenuItem(
                        icon = Icons.Default.Category,
                        iconBg = Color(0xFFF3E8FF),
                        iconColor = Color(0xFF8B5CF6),
                        title = "إدارة التصنيفات",
                        subtitle = "تصنيف الحسابات (عملاء، موردون، موظفون)",
                        onClick = { onNavigate(Screen.Categories.route) }
                    )
                }
            }

            // Section 2: السندات والعمليات
            item {
                SectionGroup(title = "السندات والحركات") {
                    MoreMenuItem(
                        icon = Icons.AutoMirrored.Filled.ReceiptLong,
                        iconBg = Color(0xFFE6F7F2),
                        iconColor = Color(0xFF16A58B),
                        title = "سندات القبض والصرف",
                        subtitle = "استعراض وإصدار السندات الرسمية",
                        onClick = { onNavigate(Screen.Vouchers.route) }
                    )
                    MoreMenuItem(
                        icon = Icons.Default.Description,
                        iconBg = Color(0xFFEEF2FF),
                        iconColor = Color(0xFF4F46E5),
                        title = "سجل كافة العمليات",
                        subtitle = "كشف العمليات المالية الشامل",
                        onClick = { onNavigate(Screen.AllTransactions.route) }
                    )
                }
            }

            // Section 3: الأمان والبيانات
            item {
                SectionGroup(title = "البيانات والنسخ الاحتياطي") {
                    MoreMenuItem(
                        icon = Icons.Default.CloudDownload,
                        iconBg = Color(0xFFE0F2FE),
                        iconColor = Color(0xFF0284C7),
                        title = "النسخ الاحتياطي واستعادة البيانات",
                        subtitle = "حفظ البيانات وتصدير ملف النسخة الاحتياطية",
                        onClick = { onNavigate(Screen.BackupRestore.route) }
                    )
                    MoreMenuItem(
                        icon = Icons.Default.Info,
                        iconBg = Color(0xFFFFF4E6),
                        iconColor = Color(0xFFF97316),
                        title = "عن التطبيق",
                        subtitle = "نظام حساباتي المالي المتطور",
                        onClick = { /* Info */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionGroup(
    title: String,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            color = DeepNavy,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(20.dp)),
            shape = RoundedCornerShape(20.dp),
            color = CardBg
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}

@Composable
private fun MoreMenuItem(
    icon: ImageVector,
    iconBg: Color,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
            contentDescription = null,
            tint = NeutralTextSecondary,
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = title,
                color = DeepNavy,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = NeutralTextSecondary,
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
