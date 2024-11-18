package com.example.citofono

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.citofono.ui.theme.CitofonoTheme
import java.io.BufferedReader
import java.io.InputStreamReader
import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.Icon
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CitofonoTheme {
        Greeting("Android")
    }
}

data class Contact(
    val id: Int,
    val name: String,
    val phoneNumber: List<String>
)

@Composable
fun ContactList(
    contacts: List<Contact>,
    onAddContact: () -> Unit,
    onCallClick: (String) -> Unit,
    onWhatsAppClick: (String) -> Unit,
    onSmsClick: (String) -> Unit
    ) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredContact = contacts.filter {contact ->
        contact.name.contains(searchQuery, ignoreCase = true) ||
        contact.phoneNumber.any { it.contains(searchQuery)}

    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Citofono") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddContact) {
                Text("+")
            }
        }
    ) { innerPadding ->
        Column (
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it},
                label = { Text("Buscar Departamento") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
            Spacer(
                modifier = Modifier
                    .height(8.dp)

            )
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)

        ) {
            if (filteredContact.isNotEmpty()) {
                items(filteredContact) { contact ->
                    ContactItem(
                        contact = contact,
                        onWhatsAppClick = onWhatsAppClick,
                        onCallClick = onCallClick,
                        onSmsClick = onSmsClick
                        )
                    }
                } else {
                        item {
                            Text(
                                text = "No se encontraron resultados",
                                style = MaterialTheme.typography.body1,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                }
            }
        }
    }
}

@Composable
fun ContactItem(
    contact: Contact,
    onCallClick: (String) -> Unit,
    onWhatsAppClick: (String) -> Unit,
    onSmsClick: (String) -> Unit
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(text = contact.name, style = MaterialTheme.typography.body1)

        contact.phoneNumber.forEach { phoneNumber: String ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "+56 $phoneNumber",
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.weight(1f)
                    )

                Row {
                    IconButton(onClick = { onCallClick(phoneNumber) }) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Llamar")
                    }
                    IconButton(onClick = {onWhatsAppClick(phoneNumber) }) {
                        Icon(imageVector = Icons.Default.Person, contentDescription = "WhatsApp")
                    }
                    IconButton(onClick = {onSmsClick(phoneNumber) }) {
                        Icon(imageVector = Icons.Default.Email, contentDescription = "Sms")
                    }
                }
            }
        }
    }
}



@Composable
fun AddContactForm(onAddContact: (Contact) -> Unit) {
    var name by remember { mutableStateOf("") }
    var currentPhoneNumber by remember { mutableStateOf("") }
    var phoneNumbers by remember { mutableStateOf(listOf<String>()) }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") }
        )
        TextField(
            value = currentPhoneNumber,
            onValueChange = { currentPhoneNumber = it },
            label = { Text("Phone Number") }
        )
        Button(onClick = {
            if (currentPhoneNumber.isNotBlank()) {
                phoneNumbers = phoneNumbers + currentPhoneNumber
                currentPhoneNumber = "" // Limpia el campo
            }
        }) {
            Text("Add Phone Number")
        }

        // Muestra la lista de números agregados
        phoneNumbers.forEach { number ->
            Text(text = "+56 $number", style = MaterialTheme.typography.body2)
        }

        Button(
            onClick = {
                if (name.isNotBlank() && phoneNumbers.isNotEmpty()) {
                    val newContact = Contact(id = 0, name = name, phoneNumber = phoneNumbers)
                    onAddContact(newContact)
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Add Contact")
        }
    }
}

fun loadContactsFromCsv(context: Context): List<Contact> {
    val contacts = mutableListOf<Contact>()
    val inputStream = context.resources.openRawResource(R.raw.contactos)
    val reader = BufferedReader(InputStreamReader(inputStream))

    reader.useLines { lines ->
        lines.forEach { line ->
            val tokens = line.split(";")
            if (tokens.size >= 4) {
                val phoneNumber = listOf(tokens[1],tokens[2])
                val contact = Contact(
                    id = contacts.size,
                    name = tokens[0],
                    phoneNumber = phoneNumber.filter { it.isNotBlank() }
                )
                contacts.add(contact)
            }
        }
    }
    return contacts
}

class MainActivity : ComponentActivity() {
    private val contacts = mutableStateListOf<Contact>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        contacts.addAll(loadContactsFromCsv(this))

        setContent {
            CitofonoTheme {
                var showAddContactForm by remember { mutableStateOf(false) }

                if (showAddContactForm) {
                    AddContactForm { newContact ->
                        contacts.add(newContact)
                        showAddContactForm = false
                    }
                } else {
                    ContactList(contacts = contacts,
                        onAddContact = {
                            showAddContactForm = true
                    },  onCallClick = { phoneNumber ->
                            val intent = Intent(Intent.ACTION_DIAL).apply { this.data = Uri.parse("tel:+56$phoneNumber") }
                            startActivity(intent)
                    },  onWhatsAppClick = { phoneNumber ->
                            val intent = Intent(Intent.ACTION_VIEW).apply { this.data = Uri.parse("https://wa.me/56$phoneNumber") }
                            startActivity(intent)
                    },  onSmsClick = { phoneNumber ->
                            val intent = Intent(Intent.ACTION_VIEW).apply { this.data = Uri.parse( "sms:+56$phoneNumber") }
                            startActivity(intent)
                    }
                    )
                }
            }
        }
    }
}
