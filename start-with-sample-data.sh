#!/bin/bash

echo "🚀 Avvio Vaadin Application con dati di esempio"
echo "================================================"
echo ""
echo "📋 Verifica configurazione..."
echo ""

# Check if we're in the right directory
if [ ! -f "pom.xml" ]; then
    echo "❌ Errore: Esegui questo script dalla directory root del progetto (vaadin-examples)"
    exit 1
fi

echo "✅ Directory corretta"
echo ""
echo "🔧 Compilazione progetto..."
echo ""

# Build the project
mvn clean package -DskipTests

if [ $? -ne 0 ]; then
    echo "❌ Errore durante la compilazione"
    exit 1
fi

echo ""
echo "✅ Compilazione completata"
echo ""
echo "🎲 Avvio con generazione dati di esempio..."
echo ""
echo "📊 Verranno creati 4 file di test:"
echo "   1. meeting_recording_2024.mp4 (con trascrizione e traduzione lunghe)"
echo "   2. interview_client_abc.mp3 (solo con trascrizione)"
echo "   3. quarterly_report_Q4.pdf (senza trascrizione/traduzione)"
echo "   4. product_demo_2024.pptx (con trascrizione e traduzione brevi)"
echo ""
echo "🌐 Accedi all'applicazione:"
echo "   URL: http://localhost:8080"
echo "   Vista dettagli: http://localhost:8080/file-details"
echo "   Vista gestione: http://localhost:8080/enhanced-files"
echo ""
echo "⏳ Avvio in corso..."
echo ""

# Run with sample-data profile
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=sample-data"
