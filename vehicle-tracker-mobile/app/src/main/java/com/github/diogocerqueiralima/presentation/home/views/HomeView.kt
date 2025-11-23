package com.github.diogocerqueiralima.presentation.home.views

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.diogocerqueiralima.R

@Composable
fun HomeView(modifier: Modifier = Modifier) {

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {

        Image(
            painter = painterResource(id = R.drawable.home_image),
            contentDescription = "Home Image",
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = 4.dp),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0057D9).copy(alpha = 0.95f), // parte de cima azul
                            Color(0xFF0057D9).copy(alpha = 0.70f),  // continua azul
                            Color(0xFF0057D9).copy(alpha = 0.40f),
                            Color(0xFF0057D9).copy(alpha = 0.30f),
                            Color.Transparent,                     // some
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.25f)        // região clara inferior
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Column(
                modifier = Modifier,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {

                Text(
                    lineHeight = 48.sp,
                    text = stringResource(id = R.string.welcome_message_title),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 50.sp
                )

                Text(
                    text = stringResource(id = R.string.welcome_message_subtitle),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 24.sp
                )

                Button(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(
                        vertical = 16.dp,
                    ),
                    colors = ButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0057D9),
                        disabledContainerColor = Color.White,
                        disabledContentColor = Color(0xFF0057D9).copy(alpha = 0.38f)
                    ),
                    shape = RoundedCornerShape(20),
                    onClick = {}
                ) {

                    Text(
                        text = "Entrar na plataforma",
                        fontSize = 20.sp
                    )

                }

            }
            
        }

    }

}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeViewPreview() {
    HomeView()
}