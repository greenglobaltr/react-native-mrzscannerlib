
Pod::Spec.new do |s|
  s.name         = "RNMrzscannerlib"
  s.version      = "1.0.4"
  s.summary      = "RNMrzscannerlib"
  s.description  = <<-DESC
                  RNMrzscannerlib
                   DESC
  s.homepage     = "asdasdad"
  s.license      = "MIT"
  # s.license      = { :type => "MIT", :file => "FILE_LICENSE" }
  s.author             = { "author" => "author@domain.cn" }
  s.platform     = :ios, "7.0"
  s.source       = { :git => "https://github.com/author/RNMrzscannerlib.git", :tag => "master" }
  s.source_files  = "/ios/RNMrzscannerlib/**/*.{h,m}"
  s.requires_arc = true


  s.dependency "React"
  #s.dependency "others"

end

  