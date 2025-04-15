
# Noema: A Language for Emotion-Driven, Logic-Aware Storytelling


## What Is Noema?

Noema is a domain-specific programming language (DSL) that enables developers to create interactive, emotionally responsive narratives by combining logical reasoning with scene scripting and emotional state modeling.

The word Noema (Greek: νόημα) means "that which is thought about"—symbolizing the core idea behind the language: characters don't just act—they think, feel, and respond based on their internal state and memories.

Noema sits at the intersection of logic programming, interactive fiction, and emotional AI scripting.

## Features

- **Emotions as First-Class Citizens**: Unlike traditional systems using rigid enums, Noema treats emotions as dynamic, mutable facts
- **Memory and Mood System**: Characters can "remember" previous events and change emotional states accordingly
- **Integrated Logic and Narrative**: Bridging programming logic with narrative design
- **Time-Based Logic**: Support for temporal conditions and reasoning
- **Scene-Based Structure**: Natural organization mirroring screenplay writing

## Project Structure

```
noema/
  ├── Token.java        # Token definitions and types
  ├── Lexer.java        # Lexical analyzer
  ├── Parser.java       # Syntax analyzer
  ├── AST.java          # Abstract Syntax Tree nodes
  ├── Interpreter.java  # Execution engine
  ├── ErrorReporter.java  # Error handling
  ├── ConditionParser.java # Complex condition parsing  
  ├── TimeBasedCondition.java # Temporal reasoning
  ├── Main.java         # Main runner class
  └── examples/         # Example Noema programs
      └── Example.noema # Simple example program
```

## Getting Started

### Prerequisites

- Java JDK 11 or higher
- Maven (optional, for building)

### Building the Project

```bash
# Clone the repository
git clone https://github.com/yourusername/noema.git
cd noema

# Compile the project
javac -d bin noema/*.java

# Or using Maven
mvn clean package
```

### Running a Noema Program

```bash
# Using the compiled classes
java -cp bin noema.Main examples/Example.noema

# Or using the JAR file
java -jar target/noema-1.0.jar examples/Example.noema
```

## Language Syntax

### Facts and Rules

Facts define the state of the world and characters:

```
fact mood("Jade", "anxious")
fact trust("Jade", "low")
```

Rules define how states change based on conditions:

```
rule reassure if player says "You did great" {
  mood("Jade") = "proud"
  trust("Jade") = "medium"
}
```

### Scenes and NPCs

Scenes contain characters and dialogue triggers:

```
scene "rooftop" {
  npc "Jade" feels mood("Jade")

  when player says "Can we trust them?" and mood("Jade") == "anxious" {
    Jade responds "I'm not sure... but we don't have a choice."
  }

  when player says "I believe in you" {
    mood("Jade") = "hopeful"
    Jade responds "Thanks... that means a lot."
  }
}
```

### Time-Based Logic

Temporal conditions allow time-based reasoning:

```
fact login("Aryan", "10:00")
rule suspicious if login(X, T1) and login(X, T2) within 1 minute {
  alert("Suspicious login detected")
}
```

## Interactive Mode

After loading a Noema program, you can interact with it in an interactive console:

```
Noema Interactive Mode
Enter a scene name to begin or 'quit' to exit
> scene:rooftop
Scene set to: rooftop
> Can we trust them?
Jade: I'm not sure... but we don't have a choice.
> I believe in you
Jade: Thanks... that means a lot.
```

## Use Cases

- **Game Development**: NPC behavior scripting in story-rich games
- **Education**: Teaching AI, emotional reasoning, or dialogue logic
- **AI Research**: Simulated conversations with mental/emotional states
- **Interactive Fiction**: Creating dynamic narrative experiences

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Inspired by logic programming languages like Prolog
- Draws concepts from interactive fiction and game narrative design
- Special thanks to all contributors and testers
