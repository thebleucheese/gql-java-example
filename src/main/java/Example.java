import graphql.ExecutionInput;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.execution.instrumentation.ChainedInstrumentation;
import graphql.execution.instrumentation.Instrumentation;
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation;
import graphql.execution.instrumentation.tracing.TracingInstrumentation;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import org.dataloader.DataLoaderRegistry;

import java.io.File;
import java.net.URISyntaxException;

import static graphql.ExecutionInput.newExecutionInput;
import static graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentationOptions.newOptions;
import static java.util.Arrays.asList;

public class Example {

	public static File loadSchemaFile(String fileName) throws URISyntaxException {
		return new File(Example.class.getResource("/" + fileName).toURI());
	}

	public static String gqlExample() throws URISyntaxException{

		//new stuff

		SchemaParser schemaParser = new SchemaParser();
		SchemaGenerator schemaGenerator = new SchemaGenerator();

		File schemaFile = loadSchemaFile("schema.graphqls");

		TypeDefinitionRegistry typeRegistry = schemaParser.parse(schemaFile);

		ExampleGraphQLContext context = new ExampleGraphQLContext();

		RuntimeWiring wiring = ExampleGraphQLWiring.buildRuntimeWiring();

		GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, wiring);


		ExecutionInput.Builder executionInput = newExecutionInput();
		executionInput.context(context);

		DataLoaderRegistry dataLoaderRegistry = context.getDataLoaderRegistry();

		DataLoaderDispatcherInstrumentation dlInstrumentation =
				new DataLoaderDispatcherInstrumentation(newOptions().includeStatistics(true));

		Instrumentation instrumentation = new ChainedInstrumentation(
				asList(new TracingInstrumentation(), dlInstrumentation));

		GraphQL build = GraphQL
				.newGraphQL(graphQLSchema)
				.instrumentation(instrumentation)
				.build();


		executionInput
				/*.query("{ accounts(ids: [\"46398d29-7d04-4f99-88fb-74d174bba6f6\"]) { id, name }, " +
						"hello }")*/
				.query("{ layers(ids: [\"860cc9bc-4da2-4698-889b-e96cca9e4bf3\"]) { id, name }, hello }")
				//.query("{ hello }")
				.dataLoaderRegistry(dataLoaderRegistry)
				.build();

		ExecutionResult executionResult = build.execute(executionInput);

		return executionResult.getData().toString();
	}

	public static void main(String[] args) throws URISyntaxException {
		System.out.println(gqlExample());
		// Prints: {hello=world}
	}
}
