import { WebTracerProvider } from '@opentelemetry/sdk-trace-web';
import { BatchSpanProcessor } from '@opentelemetry/sdk-trace-base';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http';
import { registerInstrumentations } from '@opentelemetry/instrumentation';
import { FetchInstrumentation } from '@opentelemetry/instrumentation-fetch';
import { XMLHttpRequestInstrumentation } from '@opentelemetry/instrumentation-xml-http-request';
import { UserInteractionInstrumentation } from '@opentelemetry/instrumentation-user-interaction';
import { resourceFromAttributes } from '@opentelemetry/resources';
import { ZoneContextManager } from '@opentelemetry/context-zone';

const exporter = new OTLPTraceExporter({
  // Se o SigNoz/collector estiver exposto localmente:
  url: 'http://localhost:4318/v1/traces',
});

const provider = new WebTracerProvider({
  resource: resourceFromAttributes({
    'service.name': 'frontend-angular-pix',
    'deployment.environment': 'dev',
  }),
  spanProcessors: [new BatchSpanProcessor(exporter)],
});

provider.register({
  contextManager: new ZoneContextManager(),
});

registerInstrumentations({
  instrumentations: [
    new FetchInstrumentation({
      // Restrinja aos seus endpoints reais em produção
      propagateTraceHeaderCorsUrls: [
        /http:\/\/localhost:8080\/.*/,
        /http:\/\/localhost:8081\/.*/,
      ],
    }),
    new XMLHttpRequestInstrumentation({
      propagateTraceHeaderCorsUrls: [
        /http:\/\/localhost:8080\/.*/,
        /http:\/\/localhost:8081\/.*/,
      ],
    }),
    new UserInteractionInstrumentation({
      eventNames: ['click', 'input', 'submit'],
    }),
  ],
});