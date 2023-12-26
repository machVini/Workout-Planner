package com.br.lealapps.presentation.screen

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import com.br.lealapps.data.source.model.Exercicio
import com.br.lealapps.data.source.model.Treino
import com.br.lealapps.domain.utils.toTreinoDetailData
import com.br.lealapps.presentation.screen.common.CommonNavigationBar
import com.br.lealapps.presentation.screen.common.ComposableAlertExclusion
import com.br.lealapps.presentation.viewmodel.HomeViewModel
import kotlinx.datetime.DayOfWeek
import java.util.Date

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrainingDetailScreen(
    navController: NavController,
    viewModel: HomeViewModel,
    treino: Treino,
) {
    var expanded by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var canBack by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Treino") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                },
                actions = {
                    IconButton(onClick = { expanded = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                            },
                            text = { Text(text = "Editar Treino") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Editar Treino"
                                )
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                expanded = false
                                showDialog = true
                            },
                            text = { Text(text = "Apagar Treino") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Apagar Treino"
                                )
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
        content = {
            TreinoDetailInfoItem(treino = treino, viewModel)
        },
        bottomBar = { CommonNavigationBar(navController = navController) },
    )

    if (showDialog)
        ComposableAlertExclusion(
            setShowDialog = { showDialog = it },
            title = "Apagar treino",
            subtitle = "Tem certeza que deseja deletar o treino?",
            onConfirmButton = {
                viewModel.deleteTreino(treino.nome)
                canBack = true
            }
        )

    if (canBack){
        canBack = false
        navController.popBackStack()
    }
}

@Composable
fun TreinoDetailInfoItem(treino: Treino, viewModel: HomeViewModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 72.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = treino.nome,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Descricão: ${treino.descricao}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = treino.data.toTreinoDetailData(),
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))

            LaunchedEffect(viewModel) {
                val exerciciosState: List<Exercicio> =
                    viewModel.mapListDocumentReferencesToExercicios(treino.exercicios)

                viewModel.setExerciciosState(exerciciosState)
            }

            val exerciciosState: List<Exercicio> by viewModel.exerciciosState.collectAsState(initial = emptyList())
            TrainingDetailExerciciosList(exerciciosState, viewModel)
        }
    }
}

@Composable
fun TrainingDetailExerciciosList(exercicios: List<Exercicio>, viewModel: HomeViewModel) {
    LazyColumn() {
        itemsIndexed(exercicios) { _, exercicio ->
            TrainingDetailExercicioItem(exercicio, onExercicioClick = { exercicioAtualizado ->
                viewModel.loadExercicios()
                viewModel.loadTreinos()
            })
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun TrainingDetailExercicioItem(exercicio: Exercicio, onExercicioClick: (Exercicio) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp, start = 8.dp, end = 8.dp)
            .clickable { onExercicioClick(exercicio) }
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Divider(color = MaterialTheme.colorScheme.primary, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))
        Column {
            Text(
                text = exercicio.nome,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
            )
            Spacer(modifier = Modifier.height(8.dp))
            //Image(painter = , contentDescription = )
            //Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Obs: ${exercicio.observacoes}", fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            exercicio.imagem.let { imageUrl ->
                val painter = rememberImagePainter(imageUrl)
                Image(
                    painter = painter,
                    contentDescription = "Imagem do exercício",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
    }
}



