import React, { useState, useEffect, useCallback, useRef } from 'react';
import { uploadImage } from '../api/userApi';
import { Button } from '../components/ui/button';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '../components/ui/select';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '../components/ui/card';
import { Languages, Upload, Loader2, CheckCircle2, AlertCircle, X, Image as ImageIcon } from 'lucide-react';
import { toast } from 'sonner';
import { useAuthStore } from '../store/authStore';
import AuthenticatedImage from '../components/AuthenticatedImage';

const LANGUAGES = [
  { value: 'ar', label: 'Arabic' },
  { value: 'fr', label: 'French' },
  { value: 'de', label: 'German' },
  { value: 'es', label: 'Spanish' },
  { value: 'en', label: 'English' }
];

export const DashboardPage = () => {
  const [file, setFile] = useState<File | null>(null);
  const [preview, setPreview] = useState<string | null>(null);
  const [targetLang, setTargetLang] = useState('ar');
  const [loading, setLoading] = useState(false);
  const [resultImage, setResultImage] = useState<string | null>(null);
  const { token } = useAuthStore();
  const eventSourceRef = useRef<EventSource | null>(null);

  const cleanupSSE = useCallback(() => {
    if (eventSourceRef.current) {
      eventSourceRef.current.close();
      eventSourceRef.current = null;
    }
  }, []);

  useEffect(() => {
    return () => cleanupSSE();
  }, [cleanupSSE]);

  const connectSSE = (token: string) => {
    const baseUrl = import.meta.env.VITE_BACKEND_URL || 'https://translatordomain.duckdns.org';
    fetch(`${baseUrl}/Api/User/images/stream`, {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Accept': 'text/event-stream',
      }
    }).then(response => {
      const reader = response.body?.getReader();
      const decoder = new TextDecoder();
      const read = () => {
        reader?.read().then(({ done, value }) => {
          if (done) return;
          const text = decoder.decode(value);
          const lines = text.split('\n');
          for (const line of lines) {
            if (line.startsWith('data:')) {
              try {
                const dataStr = line.replace('data:', '').trim();
                const data = JSON.parse(dataStr);
                if (data.processedUrl) {
                  setResultImage(data.processedUrl);
                  setLoading(false);
                  toast.success('Translation completed!');
                }
              } catch (e) { }
            }
          }
          read();
        });
      };
      read();
    }).catch(() => setLoading(false));
  };

  const onFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selectedFile = e.target.files?.[0];
    if (selectedFile) {
      setFile(selectedFile);
      const reader = new FileReader();
      reader.onloadend = () => setPreview(reader.result as string);
      reader.readAsDataURL(selectedFile);
      setResultImage(null);
    }
  };

  const handleUpload = async () => {
    if (!file || !token) {
      toast.error('Please select an image first');
      return;
    }

    setLoading(true);
    try {
      await uploadImage(file, targetLang);
      toast.info('Upload successful. Processing image...');
      connectSSE(token);
    } catch (error) {
      setLoading(false);
      toast.error('Upload failed');
    }
  };

  const removeFile = () => {
    setFile(null);
    setPreview(null);
    setResultImage(null);
  };

  return (
    <div className="container mx-auto px-4 py-8 max-w-4xl">
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-foreground mb-2">Image Translator</h1>
        <p className="text-muted-foreground">Upload an image and choose your target language for instant AI caption translation.</p>
      </div>

      <div className="grid md:grid-cols-2 gap-8">
        {/* Left Column: Upload */}
        <Card className="bg-card border-border">
          <CardHeader>
            <CardTitle className="text-foreground flex items-center gap-2">
              <Upload className="w-5 h-5 text-primary" />
              Upload Source
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-6">
            {!preview ? (
              <div
                onClick={() => document.getElementById('file-upload')?.click()}
                className="border-2 border-dashed border-border rounded-xl p-12 text-center cursor-pointer hover:border-primary/50 transition-colors bg-muted group"
              >
                <div className="mb-4 flex justify-center">
                  <div className="p-4 bg-primary/10 rounded-full text-primary group-hover:scale-110 transition-transform">
                    <ImageIcon className="w-8 h-8" />
                  </div>
                </div>
                <p className="text-foreground font-medium mb-1">Click to upload or drag and drop</p>
                <p className="text-sm text-muted-foreground italic">PNG, JPG or JPEG (max 5MB)</p>
                <input id="file-upload" type="file" className="hidden" accept="image/*" onChange={onFileChange} />
              </div>
            ) : (
              <div className="relative rounded-xl w-full h-400 overflow-hidden aspect-video bg-background border border-border">
                <img src={preview} alt="Upload preview" className="w-full  object-contain" />
                <button
                  onClick={removeFile}
                  className="absolute top-2 right-2 p-1 bg-black/60 hover:bg-black/80 rounded-full text-white transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
            )}

            <div className="space-y-4">
              <div className="space-y-2">
                <label className="text-sm font-medium text-foreground">Target Language</label>
                <Select value={targetLang} onValueChange={(value) => value !== null && setTargetLang(value)}>
                  <SelectTrigger className="bg-background border-border text-foreground">
                    <SelectValue placeholder="Select Language" />
                  </SelectTrigger>
                  <SelectContent className="bg-card border-border text-foreground">
                    {LANGUAGES.map((lang) => (
                      <SelectItem key={lang.value} value={lang.value}>
                        {lang.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <Button
                onClick={handleUpload}
                disabled={!file || loading}
                className="w-full bg-primary hover:bg-primary/90 h-12 text-lg font-bold shadow-lg"
              >
                {loading ? (
                  <>
                    <Loader2 className="w-5 h-5 mr-2 animate-spin" />
                    Processing...
                  </>
                ) : (
                  'Translate Image'
                )}
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Right Column: Result */}
        <Card className="bg-card border-border">
          <CardHeader>
            <CardTitle className="text-foreground flex items-center gap-2">
              <CheckCircle2 className="w-5 h-5 text-emerald-500" />
              Translation Result
            </CardTitle>
          </CardHeader>
          <CardContent className="h-full flex flex-col justify-center min-h-[300px]">
            {resultImage ? (
              <div className="rounded-xl w-full h-600  overflow-hidden bg-background border border-border h-full">
                <AuthenticatedImage url={resultImage} alt="Translated" className="w-full h-full object-contain" />
              </div>
            ) : loading ? (
              <div className="text-center py-12">
                <div className="inline-block relative">
                  <div className="w-16 h-16 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
                  <div className="absolute inset-0 flex items-center justify-center">
                    <Languages className="w-6 h-6 text-primary" />
                  </div>
                </div>
                <p className="mt-4 text-foreground font-medium">Extracting & Translating...</p>
                <p className="text-sm text-muted-foreground mt-1 italic">This may take up to 30 seconds</p>
              </div>
            ) : (
              <div className="text-center py-12 px-6">
                <AlertCircle className="w-12 h-12 text-secondary mx-auto mb-4" />
                <p className="text-muted-foreground">Translated image will appear here after processing.</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
