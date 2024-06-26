package com.spascoding.localconfig.presentation.config_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import com.spascoding.localconfig.constants.Padding
import com.spascoding.localconfig.domain.repository.model.ConfigItem
import com.spascoding.localconfig.presentation.components.BorderedListElement
import com.spascoding.localconfig.presentation.components.ConfirmDialog
import com.spascoding.localconfig.presentation.components.CustomDialog
import com.spascoding.localconfig.presentation.components.InputDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigScreen(
    viewModel: ConfigScreenViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                ),
                title = {
                    ConfigSelector()
                },
                actions = {
                    ConfigActions()
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        ParametersList(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}

@Composable
fun ConfigSelector(
    viewModel: ConfigScreenViewModel = hiltViewModel()
) {
    var mDisplayMenu by remember { mutableStateOf(false) }
    val configNames by viewModel.getAllConfigNamesUseCase().collectAsState(initial = emptyList())
    val configName = viewModel.getConfigurationFlow().collectAsState(null).value?.config

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (configNames.isNotEmpty()) {
            IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) {
                Icon(Icons.Default.ArrowDropDown, "")
            }
        }
        if (configName != null) {
            Text(
                text = configName,
            )
        }
    }

    DropdownMenu(
        expanded = mDisplayMenu,
        onDismissRequest = { mDisplayMenu = false }
    ) {
        configNames.forEach {
            DropdownMenuItem(
                text = { Text(it) },
                onClick = {
                    mDisplayMenu = false
                    viewModel.selectConfiguration(it)
                },
            )
        }
    }
}

@Composable
fun ConfigActions(
    viewModel: ConfigScreenViewModel = hiltViewModel()
) {
    var mDisplayMenu by remember { mutableStateOf(false) }
    val addConfigurationDialog = remember { mutableStateOf(false) }
    val removeConfigurationDialog = remember { mutableStateOf(false) }
    val resetConfigurationDialog = remember { mutableStateOf(false) }

    if (addConfigurationDialog.value) {
        mDisplayMenu = false
        InputDialog(
            title = "Enter configuration name",
            setShowDialog = {
                addConfigurationDialog.value = it
            }
        ) {
            viewModel.selectConfiguration(it)
        }
    }

    if (removeConfigurationDialog.value) {
        mDisplayMenu = false
        ConfirmDialog(
            text = "Do you want to delete configuration?",
            setShowDialog = {
                removeConfigurationDialog.value = it
            }
        ) {
            viewModel.deleteConfiguration()
        }
    }

    if (resetConfigurationDialog.value) {
        mDisplayMenu = false
        ConfirmDialog(
            text = "Do you want to reset configuration?",
            setShowDialog = {
                resetConfigurationDialog.value = it
            }
        ) {
            viewModel.syncFirebase()
        }
    }

    IconButton(onClick = { mDisplayMenu = !mDisplayMenu }) {
        Icon(Icons.Default.MoreVert, "")
    }

    DropdownMenu(
        expanded = mDisplayMenu,
        onDismissRequest = { mDisplayMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("Add configuration") },
            onClick = { addConfigurationDialog.value = true },
            trailingIcon = { Icons.Default.Add }
        )
        DropdownMenuItem(
            text = { Text("Remove configuration") },
            onClick = { removeConfigurationDialog.value = true },
            trailingIcon = { Icons.Default.Delete }
        )
        DropdownMenuItem(
            text = { Text("Reset configuration") },
            onClick = {
                resetConfigurationDialog.value = true
            },
            trailingIcon = { Icons.Default.Refresh }
        )
    }
}

@Composable
fun ParametersList(
    modifier: Modifier,
    viewModel: ConfigScreenViewModel = hiltViewModel(),
) {
    val configItems by viewModel.getConfiguration().collectAsState(initial = emptyList())

    val editParameterDialog = remember { mutableStateOf(false) }
    val configItemTemp = remember { mutableStateOf(ConfigItem("", "", "")) }

    if (editParameterDialog.value) {
        CustomDialog(
            parameter = configItemTemp.value.parameter,
            value = configItemTemp.value.value,
            setShowDialog = {
                editParameterDialog.value = it
            }
        ) {
            if (it.isNotBlank()) {
                viewModel.setParameter(configItemTemp.value.copy(value = it))
            }
        }
    }

    LazyColumn(
        modifier = modifier
    ) {
        configItems.forEach { configItem ->
            item {
                BorderedListElement(
                    onClickItem = {
                        editParameterDialog.value = true
                        configItemTemp.value = configItem
                    }
                ) {
                    Column(
                        modifier = Modifier.padding(Padding.MEDIUM)
                    ) {
                        Text(
                            modifier = Modifier.padding(start = Padding.SMALL),
                            text = configItem.parameter,
                            fontWeight = FontWeight.Bold,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = Padding.SMALL),
                                text = configItem.value,
                            )
                            Icon(
                                modifier = Modifier,
                                imageVector = Icons.Default.Edit,
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        }
    }
}