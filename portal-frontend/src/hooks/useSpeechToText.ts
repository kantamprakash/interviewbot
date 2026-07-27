import { useEffect, useRef, useState } from 'react';

const SpeechRecognitionCtor: any =
  (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition;

export default function useSpeechToText(onFinalTranscript: (text: string) => void) {
  const [isListening, setIsListening] = useState(false);
  const [interimTranscript, setInterimTranscript] = useState('');
  const recognitionRef = useRef<any>(null);
  const stoppedDeliberatelyRef = useRef(true);
  const onFinalTranscriptRef = useRef(onFinalTranscript);
  onFinalTranscriptRef.current = onFinalTranscript;

  useEffect(() => {
    return () => {
      stoppedDeliberatelyRef.current = true;
      recognitionRef.current?.stop();
    };
  }, []);

  const startListening = () => {
    if (!SpeechRecognitionCtor || recognitionRef.current) return;

    const recognition = new SpeechRecognitionCtor();
    recognition.continuous = true;
    recognition.interimResults = true;
    recognition.lang = 'en-US';

    recognition.onresult = (event: any) => {
      let interim = '';
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const result = event.results[i];
        if (result.isFinal) {
          onFinalTranscriptRef.current(result[0].transcript.trim());
        } else {
          interim += result[0].transcript;
        }
      }
      setInterimTranscript(interim);
    };

    recognition.onerror = (event: any) => {
      console.warn('Speech recognition error:', event.error);
    };

    recognition.onend = () => {
      if (!stoppedDeliberatelyRef.current) {
        // Chrome stops continuous recognition after a pause in speech; restart it.
        recognition.start();
        return;
      }
      recognitionRef.current = null;
      setIsListening(false);
      setInterimTranscript('');
    };

    stoppedDeliberatelyRef.current = false;
    recognitionRef.current = recognition;
    recognition.start();
    setIsListening(true);
  };

  const stopListening = () => {
    stoppedDeliberatelyRef.current = true;
    recognitionRef.current?.stop();
  };

  return {
    isSupported: !!SpeechRecognitionCtor,
    isListening,
    interimTranscript,
    startListening,
    stopListening,
  };
}
