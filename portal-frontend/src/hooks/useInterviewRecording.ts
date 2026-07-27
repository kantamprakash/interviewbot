import { useEffect, useRef, useState } from 'react';

const CANDIDATE_MIME_TYPES = ['video/webm;codecs=vp8,opus', 'video/webm'];

function pickSupportedMimeType(): string | null {
  for (const mimeType of CANDIDATE_MIME_TYPES) {
    if (window.MediaRecorder && MediaRecorder.isTypeSupported(mimeType)) {
      return mimeType;
    }
  }
  return null;
}

export default function useInterviewRecording() {
  const [isRecording, setIsRecording] = useState(false);
  const streamRef = useRef<MediaStream | null>(null);
  const recorderRef = useRef<MediaRecorder | null>(null);
  const chunksRef = useRef<Blob[]>([]);
  const mimeTypeRef = useRef<string>('video/webm');

  useEffect(() => {
    let cancelled = false;

    const start = async () => {
      try {
        const mimeType = pickSupportedMimeType();
        if (!mimeType) {
          console.warn('No supported video recording format found; skipping recording.');
          return;
        }

        const stream = await navigator.mediaDevices.getUserMedia({
          video: { width: 640, height: 480 },
          audio: true,
        });
        if (cancelled) {
          stream.getTracks().forEach((track) => track.stop());
          return;
        }

        streamRef.current = stream;
        mimeTypeRef.current = mimeType;

        const recorder = new MediaRecorder(stream, {
          mimeType,
          videoBitsPerSecond: 250_000,
          audioBitsPerSecond: 64_000,
        });
        recorder.ondataavailable = (event: BlobEvent) => {
          if (event.data && event.data.size > 0) {
            chunksRef.current.push(event.data);
          }
        };
        recorder.start(1000);
        recorderRef.current = recorder;
        setIsRecording(true);
      } catch (err) {
        console.warn('Camera/microphone recording unavailable, continuing without it:', err);
      }
    };

    start();

    return () => {
      cancelled = true;
      recorderRef.current?.stop();
      streamRef.current?.getTracks().forEach((track) => track.stop());
    };
  }, []);

  const stopAndGetBlob = (): Promise<Blob | null> => {
    const recorder = recorderRef.current;
    if (!recorder || recorder.state === 'inactive') {
      return Promise.resolve(null);
    }

    return new Promise((resolve) => {
      recorder.onstop = () => {
        streamRef.current?.getTracks().forEach((track) => track.stop());
        setIsRecording(false);
        if (chunksRef.current.length === 0) {
          resolve(null);
          return;
        }
        resolve(new Blob(chunksRef.current, { type: mimeTypeRef.current }));
      };
      recorder.stop();
    });
  };

  return { isRecording, stopAndGetBlob };
}
